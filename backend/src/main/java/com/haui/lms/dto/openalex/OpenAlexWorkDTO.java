package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenAlexWorkDTO {
    private String id;

    private String doi;

    @JsonProperty("publication_year")
    private Integer publicationYear;

    private String type;

    private List<AuthorshipDTO> authorships;
}
