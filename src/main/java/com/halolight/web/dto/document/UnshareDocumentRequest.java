package com.halolight.web.dto.document;

import lombok.Data;

import java.util.List;

@Data
public class UnshareDocumentRequest {
    private List<String> userIds;
    private List<String> teamIds;
}
