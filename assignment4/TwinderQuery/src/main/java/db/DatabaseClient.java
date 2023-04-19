package db;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import javax.naming.ldap.SortKey;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseClient {

//    private static final String SERVICE_ENDPOINT = "http://localhost:8000";
    private Region region;
    private String tableName;
    private DynamoDbClient client;
    private DynamoDbEnhancedClient enhancedClient;

    public DatabaseClient(Region region, String tableName) {
        this.region = region;
        this.tableName = tableName;
        this.client = DynamoDbClient.builder()
//                .endpointOverride(URI.create(SERVICE_ENDPOINT))
                .region(region)
                .build();

        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();

        createTableIfNotExists(tableName);
    }

    public DynamoDbTable<SwipeADO> getSwipeDataTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(SwipeADO.class));
    }

    public void createTableIfNotExists(String tableName) {

        DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();

        try {
            DescribeTableResponse tableDescription = client.describeTable(request);

            // table exists, so we are done
            System.out.println("Table " + tableDescription.table().tableName() + " already exists.");

        } catch (ResourceNotFoundException e) {

            DynamoDbTable<SwipeADO> mappedTable =
                    enhancedClient.table(tableName, TableSchema.fromBean(SwipeADO.class));

            // Cceate the table
            mappedTable.createTable(builder -> builder
                    .provisionedThroughput(b -> b
                            .readCapacityUnits(20L)
                            .writeCapacityUnits(6000L)
                            .build()));

            System.out.println("Waiting for table creation...");

            // waiting for table to be created
            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder()
                    .overrideConfiguration(b -> b.maxAttempts(10))
                    .client(client)
                    .build()) { // DynamoDbWaiter is Autocloseable

                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(tableName).build())
                        .matched();

                DescribeTableResponse tableDescriptionRes = response.response().orElseThrow(
                        () -> new RuntimeException("Customer table was not created."));

                // The actual error can be inspected in response.exception()
                System.out.println(tableDescriptionRes.table().tableName() + " was created.");
            }
        }
    }

    public SwipeADO getCount(DynamoDbTable<SwipeADO> table, String itemId) {

        SwipeADO result;

        try {
            Key key = Key.builder()
                    .partitionValue(itemId)
                    .sortValue("count")
                    .build();

            // Get the item by using the key.
            result = table.getItem(key);
//            System.out.println("******* The description value is " + result.toString());

            return result;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public List<String> getMatches(String itemId) {

        try {
            QueryRequest likeRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("id = :pk and begins_with(identifier, :sk)")
                    .expressionAttributeValues(Map.of(":pk", AttributeValue.builder().s(itemId).build(),
                            ":sk", AttributeValue.builder().s("like_").build()))
                    .projectionExpression("otherUserId")
                    .limit(100)
                    .build();

            // Get the item by using the key.
            return client.query(likeRequest).items()
                    .stream().map(Map::values).flatMap(Collection::stream).map(AttributeValue::s)
                    .collect(Collectors.toList());

            // If you want the exact matches
//            QueryRequest isLikedRequest = QueryRequest.builder()
//                    .tableName(tableName)
//                    .keyConditionExpression("id = :pk and begins_with(identifier, :sk)")
//                    .expressionAttributeValues(Map.of(":pk", AttributeValue.builder().s(itemId).build(),
//                            ":sk", AttributeValue.builder().s("isLiked_").build()))
//                    .projectionExpression("otherUserId")
//                    .build();

//            List<String> out = new LinkedList<>();
//
//            client.query(isLikedRequest).items().forEach(
//                isLiked ->
//                {
//                    String id = isLiked.get("otherUserId").s();
//                    if (likeResult.contains(id)) {
//                        out.add(id);
//                    }
//                }
//            );
//
//            return out;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void scan() {
        try{
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(client)
                    .build();

            DynamoDbTable<SwipeADO> table = enhancedClient.table(tableName, TableSchema.fromBean(SwipeADO.class));
            Iterator<SwipeADO> results = table.scan().items().iterator();
            while (results.hasNext()) {
                SwipeADO rec = results.next();
                System.out.println("The record id is "+rec.getId());
                System.out.println("The name is " +rec.toString());
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done");
    }

    public void deleteDynamoDBTable() {

        DeleteTableRequest request = DeleteTableRequest.builder()
                .tableName(tableName)
                .build();

        try {
            client.deleteTable(request);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println(tableName +" was successfully deleted!");
    }
}

