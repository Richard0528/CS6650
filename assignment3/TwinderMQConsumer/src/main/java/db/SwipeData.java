package db;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.*;
import java.util.stream.Collectors;

@DynamoDbBean
public class SwipeData {

    private String id;

    private int likeCnt;

    private int dislikeCnt;

    private Set<String> like;

    private Set<String> beingLiked;

    public SwipeData() {
        this.likeCnt = 0;
        this.dislikeCnt = 0;
        this.like = new HashSet<>(){{ add(""); }};
        this.beingLiked = new HashSet<>(){{ add(""); }};
    }

    public SwipeData(String id) {
        this.id = id;
        this.likeCnt = 0;
        this.dislikeCnt = 0;
        this.like = new HashSet<>(){{ add(""); }};
        this.beingLiked = new HashSet<>(){{ add(""); }};
    }

    public SwipeData(String id, int likeCnt, int dislikeCnt, Set<String> like, Set<String> beingLiked) {
        this.id = id;
        this.likeCnt = likeCnt;
        this.dislikeCnt = dislikeCnt;
        this.like = like;
        this.beingLiked = beingLiked;
    }

    public String getId() {
        return id;
    }

    @DynamoDbPartitionKey
    public void setId(String id) {
        this.id = id;
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

    public Set<String> getLike() {
        return like;
    }

    public void setLike(Set<String> like) {
        this.like = like;
    }

    public void addLike(String id) {
        this.like.add(id);
    }

    public Set<String> getBeingLiked() {
        return beingLiked;
    }

    public void setBeingLiked(Set<String> beingLiked) {
        this.beingLiked = beingLiked;
    }

    public void addBeingLiked(String id) {
        this.beingLiked.add(id);
    }

    public List<String> getMatches() {
        List<String> out = new LinkedList<>();

        for (String l : like) {
            if (beingLiked.contains(l)) {
                out.add(l);
            }
        }

        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeData swipeData = (SwipeData) o;
        return likeCnt == swipeData.likeCnt && dislikeCnt == swipeData.dislikeCnt && Objects.equals(id, swipeData.id) && Objects.equals(like, swipeData.like) && Objects.equals(beingLiked, swipeData.beingLiked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, likeCnt, dislikeCnt, like, beingLiked);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipeData{");
        sb.append("id='").append(id).append('\'');
        sb.append(", likeCnt=").append(likeCnt);
        sb.append(", dislikeCnt=").append(dislikeCnt);
        sb.append(", like=").append(like);
        sb.append(", beingLiked=").append(beingLiked);
        sb.append('}');
        return sb.toString();
    }
}

