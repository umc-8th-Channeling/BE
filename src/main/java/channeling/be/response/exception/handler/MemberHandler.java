package channeling.be.response.exception.handler;

import java.util.Map;

import channeling.be.response.code.BaseErrorCode;
import channeling.be.response.exception.GeneralException;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
