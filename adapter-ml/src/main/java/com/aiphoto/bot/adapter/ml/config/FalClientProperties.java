package com.aiphoto.bot.adapter.ml.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fal")
public class FalClientProperties {

    @NotBlank
    private String apiBase;
    private String apiKey;
    private int timeoutSec = 120;
    private Training training = new Training();
    private Generation generation = new Generation();
    private Realism realism = new Realism();
    private Business business = new Business();

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutSec() {
        return timeoutSec;
    }

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    public Training getTraining() {
        return training;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

    public Realism getRealism() {
        return realism;
    }

    public void setRealism(Realism realism) {
        this.realism = realism;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public static class Training {
        @NotBlank
        private String endpoint;
        private int steps = 1600;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }
    }

    public static class Generation {
        @NotBlank
        private String endpoint;
        private int width = 896;
        private int height = 1152;
        private int steps = 25;
        private double guidance = 3.5;
        private double loraScale = 0.6;
        private Long seed;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public double getGuidance() {
            return guidance;
        }

        public void setGuidance(double guidance) {
            this.guidance = guidance;
        }

        public double getLoraScale() {
            return loraScale;
        }

        public void setLoraScale(double loraScale) {
            this.loraScale = loraScale;
        }

        public Long getSeed() {
            return seed;
        }

        public void setSeed(Long seed) {
            this.seed = seed;
        }
    }

    public static class Realism {
        @NotBlank
        private String endpoint;
        private int steps = 12;
        private double strength = 0.65;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public double getStrength() {
            return strength;
        }

        public void setStrength(double strength) {
            this.strength = strength;
        }
    }

    public static class Business {
        private int minPhotos = 10;
        private int maxPhotos = 30;
        private int imagesPerRequest = 3;
        private int trainingEtaMin = 5;

        public int getMinPhotos() {
            return minPhotos;
        }

        public void setMinPhotos(int minPhotos) {
            this.minPhotos = minPhotos;
        }

        public int getMaxPhotos() {
            return maxPhotos;
        }

        public void setMaxPhotos(int maxPhotos) {
            this.maxPhotos = maxPhotos;
        }

        public int getImagesPerRequest() {
            return imagesPerRequest;
        }

        public void setImagesPerRequest(int imagesPerRequest) {
            this.imagesPerRequest = imagesPerRequest;
        }

        public int getTrainingEtaMin() {
            return trainingEtaMin;
        }

        public void setTrainingEtaMin(int trainingEtaMin) {
            this.trainingEtaMin = trainingEtaMin;
        }
    }
}
