package model;

import java.util.Objects;

public class SwipeStat {

    private int likeCnt;

    private int dislikeCnt;

    public SwipeStat() {
        this.likeCnt = 0;
        this.dislikeCnt = 0;
    }

    public SwipeStat(int likeCnt, int dislikeCnt) {
        this.likeCnt = likeCnt;
        this.dislikeCnt = dislikeCnt;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }

    public void incrementLikeCnt() {
        this.likeCnt++;
    }

    public int getDislikeCnt() {
        return dislikeCnt;
    }

    public void setDislikeCnt(int dislikeCnt) {
        this.dislikeCnt = dislikeCnt;
    }

    public void incrementDislikeCnt() {
        this.dislikeCnt++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeStat swipeStat = (SwipeStat) o;
        return likeCnt == swipeStat.likeCnt && dislikeCnt == swipeStat.dislikeCnt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(likeCnt, dislikeCnt);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipeStat{");
        sb.append("likeCnt=").append(likeCnt);
        sb.append(", dislikeCnt=").append(dislikeCnt);
        sb.append('}');
        return sb.toString();
    }
}
