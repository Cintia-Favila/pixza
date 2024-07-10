package org.switf.pixza.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long idCategory;
    private String category;
    private String user;

    public CategoryResponse(Long idCategory, String category) {
        this.idCategory = idCategory;
        this.category = category;
    }
}
