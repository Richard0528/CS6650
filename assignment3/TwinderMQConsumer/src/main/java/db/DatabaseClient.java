package db;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

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

    public DynamoDbTable<SwipeData> getSwipeDataTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(SwipeData.class));
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

            DynamoDbTable<SwipeData> mappedTable =
                    enhancedClient.table(tableName, TableSchema.fromBean(SwipeData.class));

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

    /**
     * Create or update items based on the given list of @SwipeData
     * Note: the update action here is overwriting the existing items
     *
     * @param items
     */
    public void putOrUpdateItems(DynamoDbTable<SwipeData> table, List<SwipeData> items) {

        // Create a WriteBatch object and add all the WriteRequest objects to it
        WriteBatch.Builder<SwipeData> writeBatchBuilder = WriteBatch.builder(SwipeData.class).mappedTableResource(table);

        // Create a list of WriteRequest objects for each item to be written
        items.forEach(writeBatchBuilder::addPutItem);

        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
                BatchWriteItemEnhancedRequest.builder().writeBatches(writeBatchBuilder.build()).build();

        enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }

    public String getItem(DynamoDbEnhancedClient enhancedClient) {

        SwipeData result = null;

        try {

            DynamoDbTable<SwipeData> table = enhancedClient.table(tableName, TableSchema.fromBean(SwipeData.class));
            Key key = Key.builder()
                    .partitionValue("12121212")
                    .build();

            // Get the item by using the key.
            result = table.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            System.out.println("******* The description value is " + result.toString());

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return result.toString();
    }

    public void scan() {
        try{
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(client)
                    .build();

            DynamoDbTable<SwipeData> table = enhancedClient.table(tableName, TableSchema.fromBean(SwipeData.class));
            Iterator<SwipeData> results = table.scan().items().iterator();
            while (results.hasNext()) {
                SwipeData rec = results.next();
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

