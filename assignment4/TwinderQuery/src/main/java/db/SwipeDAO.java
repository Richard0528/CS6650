package db;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.Objects;

@DynamoDbBean
public class SwipeDAO {

    private String id;

    /**
     * The secondary key to identify what kind of data this row represents
     * 1. like count and dislike count               -- count
     * 2. current user like the other user           -- like_<other userId>
     * 3. current user is liked by the other user    -- isLiked_<other userId>
     */
    private String identifier;

    private int likeCnt;

    private int dislikeCnt;

    private String otherUserId;

    public SwipeDAO() {
        this.likeCnt = 0;
        this.dislikeCnt = 0;
    }

    public SwipeDAO(String id, String identifier) {
        this.id = id;
        this.identifier = identifier;
        this.likeCnt = 0;
        this.dislikeCnt = 0;
    }

    public SwipeDAO(String id, String identifier, String otherUserId) {
        this.id = id;
        this.identifier = identifier;
        this.otherUserId = otherUserId;
    }

    public SwipeDAO(String id, String identifier, int likeCnt, int dislikeCnt) {
        this.id = id;
        this.identifier = identifier;
        this.likeCnt = likeCnt;
        this.dislikeCnt = dislikeCnt;
    }

    public SwipeDAO(String id, String identifier, int likeCnt, int dislikeCnt, String otherUserId) {
        this.id = id;
        this.identifier = identifier;
        this.likeCnt = likeCnt;
        this.dislikeCnt = dislikeCnt;
        this.otherUserId = otherUserId;
    }

    public String getId() {
        return id;
    }

    @DynamoDbPartitionKey
    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    @DynamoDbSortKey
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeDAO swipeDAO = (SwipeDAO) o;
        return likeCnt == swipeDAO.likeCnt && dislikeCnt == swipeDAO.dislikeCnt && Objects.equals(id, swipeDAO.id) && Objects.equals(identifier, swipeDAO.identifier) && Objects.equals(otherUserId, swipeDAO.otherUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, identifier, likeCnt, dislikeCnt, otherUserId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwipeDAO{");
        sb.append("id='").append(id).append('\'');
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", likeCnt=").append(likeCnt);
        sb.append(", dislikeCnt=").append(dislikeCnt);
        sb.append(", otherUserId='").append(otherUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

