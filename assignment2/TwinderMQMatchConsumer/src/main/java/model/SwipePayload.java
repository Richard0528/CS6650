package model;

import java.io.Serializable;
import java.util.Objects;

public class SwipePayload extends SwipeDetails implements Serializable {

    private boolean like;

    public SwipePayload(String swiper, String swipee, String comment, boolean like) {
        super(swiper, swipee, comment);
        this.like = like;
    }

    public SwipePayload(SwipeDetails swipeDetails, boolean like) {
        super(swipeDetails.getSwiper(), swipeDetails.getSwipee(), swipeDetails.getComment());
        this.like = like;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SwipePayload that = (SwipePayload) o;
        return like == that.like;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), like);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipePayload{");
        sb.append("swiper='").append(super.getSwiper()).append('\'');
        sb.append(", swipee='").append(super.getSwipee()).append('\'');
        sb.append(", comment='").append(super.getComment()).append('\'');
        sb.append(", like=").append(like);
        sb.append('}');
        return sb.toString();
    }
}
