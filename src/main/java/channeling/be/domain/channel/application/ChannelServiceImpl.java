package channeling.be.domain.channel.application;

import channeling.be.domain.channel.application.model.Stats;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.ChannelHashTag;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.channel.presentation.converter.ChannelConverter;
import channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.application.VideoService;
import channeling.be.global.infrastructure.redis.RedisUtil;
import channeling.be.global.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
import channeling.be.global.infrastructure.youtube.YoutubeUtil;
import channeling.be.global.infrastructure.youtube.dto.model.YoutubeVideoBriefDTO;
import channeling.be.global.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;
import channeling.be.response.exception.handler.ChannelHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_FOUND;
import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_MEMBER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class ChannelServiceImpl implements ChannelService {
	private final ChannelRepository channelRepository;
	private final VideoService videoService;
	private final RedisUtil redisUtil;

	@AllArgsConstructor
	@Getter
	private static class YoutubeChannelData {
		YoutubeChannelResDTO.Item item;
		List<YoutubeVideoBriefDTO> briefs;
		List<YoutubeVideoDetailDTO> details;
	}

	@Override
	@Transactional
	public Channel editChannelConcept(Long channelId, ChannelRequestDto.EditChannelConceptReqDto request) {
		Channel channel = channelRepository.findById(channelId)
			.orElseThrow(() -> new ChannelHandler(
				_CHANNEL_NOT_FOUND)); // id로 채널 조회 -> 추후 로그인 멤버 가져온 후, 멤버로 조회하는 걸로 바궈야 할 듯..? 일대일이니까..
		channel.editConcept(request.getConcept()); // 더티체킹
		return channel;
	}

	@Override
	public void validateChannelByIdAndMember(Long channelId) {
		boolean isExist = channelRepository.existsById(channelId);
		if (!isExist) {
			throw new ChannelHandler(_CHANNEL_NOT_FOUND);
		}
		//TODO: 추후 유저 + 채널 연관 관계 확인 로직 필요
	}

	@Override
	@Transactional
	public Channel editChannelTarget(Long channelId, ChannelRequestDto.EditChannelTargetReqDto request) {
		Channel channel = channelRepository.findById(channelId)
			.orElseThrow(() -> new ChannelHandler(
				_CHANNEL_NOT_FOUND));// id로 채널 조회 -> 추후 로그인 멤버 가져온 후, 멤버로 조회하는 걸로 바궈야 할 듯..? 일대일이니까..
		channel.editTarget(request.getTarget()); // 더티체킹
		return channel;
	}

	@Override
	@Transactional
	public void updateChannelVideos(Channel channel, String youtubeAccessToken) {
		// List<YoutubeVideoBriefDTO> stats = YoutubeUtil.getVideoStatistics(youtubeAccessToken, channel.getJoinDate(),
		// 	LocalDateTime.now());
		// if (stats.isEmpty()) {
		// 	log.warn("채널 {}의 비디오 통계가 없습니다.", channel.getName());
		// 	return;
		// }
	}

	@Override
	@Transactional
	public Channel updateOrCreateChannelByMember(Member member) {
		Optional<Channel> channel = channelRepository.findByMember(member);
		String googleAccessToken = redisUtil.getGoogleAccessToken(member.getId());


		// 유튜브 채널 정보 가져오기
		YoutubeChannelResDTO.Item item = YoutubeUtil.getChannelDetails(
			redisUtil.getGoogleAccessToken(member.getId()));
		String playlistId = item.getContentDetails().getRelatedPlaylists().getUploads();

		YoutubeChannelData data = fetchYoutubeVideoData(item, googleAccessToken, playlistId);

		Channel channelEntity = channel.orElseGet(() ->
			channelRepository.save(ChannelConverter.toNewChannel(data.item, member))
		);
		Stats stats=updateVideosAndAccumulateStats(data.briefs, data.details, channelEntity);
		ChannelConverter.updateChannel(channelEntity, data.item, stats);
		return channelRepository.save(channelEntity);

	}

	@Override
	public Channel getChannel(Long channelId, Member loggedInMember) {
		Channel channel = channelRepository.findById(channelId)
			.orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

		if (!channel.getMember().getId().equals(loggedInMember.getId())) {
			throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
		}

		return channel;
	}

	private Stats updateVideosAndAccumulateStats(List<YoutubeVideoBriefDTO> briefs, List<YoutubeVideoDetailDTO> details, Channel channel) {
		long likeCount = 0, commentCount = 0;

		for (int i = 0; i < briefs.size(); i++) {
			YoutubeVideoBriefDTO brief = briefs.get(i);
			YoutubeVideoDetailDTO detail = details.get(i);
			likeCount += detail.getLikeCount();
			commentCount += detail.getCommentCount();
			videoService.updateVideo(brief, detail, channel);
		}
		return new Stats(likeCount, commentCount);
	}


	private YoutubeChannelData fetchYoutubeVideoData(
		YoutubeChannelResDTO.Item item,
		String accessToken,
		String uploadPlaylistId
	) {
		List<YoutubeVideoBriefDTO> videoBriefs = YoutubeUtil.getVideosBriefsByPlayListId(accessToken, uploadPlaylistId);
		List<YoutubeVideoDetailDTO> videoDetails = YoutubeUtil.getVideoDetailsByIds(
			accessToken, videoBriefs.stream().map(YoutubeVideoBriefDTO::getVideoId).toList());

		return new YoutubeChannelData(item, videoBriefs, videoDetails);
	}
}

