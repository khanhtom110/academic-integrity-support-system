package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AuthorshipDTO {
    @JsonProperty("author_position")
    private String authorPosition;

    private AuthorDTO author;

    private List<InstitutionDTO> institutions;

    private List<String> countries;

    @JsonProperty("is_corresponding")
    private Boolean isCorresponding;
}
