package model;

import java.io.Serializable;
import java.util.Objects;

public class SwipeDetails implements Serializable {

    private String swiper;

    private String swipee;

    private String comment;

    public SwipeDetails(String swiper, String swipee, String comment) {
        this.swiper = swiper;
        this.swipee = swipee;
        this.comment = comment;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public String getSwipee() {
        return swipee;
    }

    public void setSwipee(String swipee) {
        this.swipee = swipee;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isValid(){
        return isStrValid(getSwiper()) && isStrValid(getSwipee());
    }

    private boolean isStrValid(String str) {
        return str != null && !str.isBlank() && !str.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeDetails that = (SwipeDetails) o;
        return Objects.equals(swiper, that.swiper) && Objects.equals(swipee, that.swipee) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(swiper, swipee, comment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipeDetails{");
        sb.append("swiper='").append(swiper).append('\'');
        sb.append(", swipee='").append(swipee).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
