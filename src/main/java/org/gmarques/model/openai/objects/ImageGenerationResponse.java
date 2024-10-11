package org.gmarques.model.openai.objects;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageGenerationResponse {
    public long created;
    public List<ImageData> data;
}
