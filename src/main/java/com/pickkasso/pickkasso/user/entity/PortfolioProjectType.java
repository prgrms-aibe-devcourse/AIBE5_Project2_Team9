package com.pickkasso.pickkasso.user.entity;

public enum PortfolioProjectType {
    PERSONAL,
    COMMERCIAL;

    public String getDisplayName() {
        return switch (this) {
            case PERSONAL -> "개인";
            case COMMERCIAL -> "상업 프로젝트";
        };
    }
}
