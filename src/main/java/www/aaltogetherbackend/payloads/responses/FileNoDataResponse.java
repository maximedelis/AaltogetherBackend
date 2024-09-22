package www.aaltogetherbackend.payloads.responses;

public record FileNoDataResponse(
        long id,
        String name,
        String type,
        String uploader
) {}
