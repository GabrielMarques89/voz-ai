package org.gmarques.model.openai.objects;

public class ImageGenerationRequest {
    public String model;
    public String prompt;
    public int n;
    public String size;

    public ImageGenerationRequest(String model, String prompt, int n, String size) {
        this.model = model;
        this.prompt = prompt;
        this.n = n;
        this.size = size;
    }
}
