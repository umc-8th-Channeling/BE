package channeling.be.domain.report.domain;

import channeling.be.domain.common.BaseEntity;
import channeling.be.domain.video.domain.Video;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column
    private String title; // 영상 제목

    @Column
    private Long view; // 조회수

    @Column
    private Long viewTopicAvg; // 동일 주제 평균 조회수

    @Column
    private Long viewChannelAvg; // 체널 평균 조회수

    @Column
    private Long likeCount; // 좋아요 수

    @Column
    private Long likeTopicAvg; // 동일 주제 평균 좋아요 수

    @Column
    private Long likeChannelAvg; // 채널 평균 좋아요 수

    @Column
    private Long comment; // 댓글 수

    @Column
    private Long commentTopicAvg; // 동일 주제 평균 댓글 수

    @Column
    private Long commentChannelAvg; // 채널 평균 좋아요 수

    @Column
    private Integer concept; //컨셉 일관성

    @Column
    private Long seo; // seo 구성

    @Column
    private Long revisit; // 재방문률

    @Column
    private String summary; // 요약본

    @Column
    private Long neutralComment; // 중립 댓글 수

    @Column
    private Long adviceComment; // 조언 댓글 수

    @Column
    private Long positiveComment; // 긍정 댓글 수

    @Column
    private Long negativeComment; // 부정 댓글 수

    @Column
    private String leaveAnalyze; // 시청자 이탈 분석

    @Column
    private String optimization; // 알고리즘 최적화

}
