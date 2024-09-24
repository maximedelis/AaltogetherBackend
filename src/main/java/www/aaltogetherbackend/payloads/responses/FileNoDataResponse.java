package www.aaltogetherbackend.payloads.responses;

public interface FileNoDataResponse {
    Long getId();
    String getName();
    String getType();
    UserInfoResponse getUploader();
}
