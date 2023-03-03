package model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SwipeMatchStat {

    private HashSet<String> like;

    private HashSet<String> beingLiked;

    public SwipeMatchStat() {
        this.like = new HashSet<>();
        this.beingLiked = new HashSet<>();
    }

    public HashSet<String> getLike() {
        return like;
    }

    public void addLike(String id) {
        this.like.add(id);
    }

    public HashSet<String> getBeingLiked() {
        return beingLiked;
    }

    public void addBeingLiked(String id) {
        this.beingLiked.add(id);
    }

    public List<String> getMatches() {
        Set<String> likeCopy = new HashSet(like);
        likeCopy.retainAll(beingLiked);

        return likeCopy.stream().limit(100).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeMatchStat that = (SwipeMatchStat) o;
        return Objects.equals(like, that.like) && Objects.equals(beingLiked, that.beingLiked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(like, beingLiked);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipeMatchStat{");
        sb.append("like=").append(like);
        sb.append(", beingLiked=").append(beingLiked);
        sb.append('}');
        return sb.toString();
    }
}
