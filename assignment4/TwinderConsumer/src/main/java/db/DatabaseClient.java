package db;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbAsyncWaiter;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class DatabaseClient {

//    private static final String SERVICE_ENDPOINT = "http://localhost:8000";
    private Region region;
    private String tableName;
    private DynamoDbAsyncClient client;
    private DynamoDbEnhancedAsyncClient enhancedClient;

    public DatabaseClient(Region region, String tableName) {
        this.region = region;
        this.tableName = tableName;
        this.client = DynamoDbAsyncClient.builder()
//                .endpointOverride(URI.create(SERVICE_ENDPOINT))
                .region(region)
                .build();

        this.enhancedClient = DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build();

        try {
            // Create Command table
            createTableIfNotExists(tableName, 1L, 6000L);
            // Create Query table
            createTableIfNotExists(tableName + "_query", 1000L, 6000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public DynamoDbAsyncTable<SwipeADO> getSwipeDataTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(SwipeADO.class));
    }

    private void createTableIfNotExists(String tableName, long readCapacity, long writeCapacity) throws InterruptedException {

        DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();

        try {
            DescribeTableResponse tableDescription = client.describeTable(request).join();

            // table exists, so we are done
            System.out.println("Table " + tableDescription.table().tableName() + " already exists.");

        } catch (CompletionException | ResourceNotFoundException e) {

            DynamoDbAsyncTable<SwipeADO> mappedTable =
                    enhancedClient.table(tableName, TableSchema.fromBean(SwipeADO.class));

            // Cceate the table
            mappedTable.createTable(builder -> builder
                    .provisionedThroughput(b -> b
                            .readCapacityUnits(readCapacity)
                            .writeCapacityUnits(writeCapacity)
                            .build()));

            System.out.println("Waiting for table creation...");

            // waiting for table to be created
            try (DynamoDbAsyncWaiter waiter = DynamoDbAsyncWaiter.builder()
                    .overrideConfiguration(b -> b.maxAttempts(10))
                    .client(client)
                    .build();
            ) { // DynamoDbWaiter is Autocloseable

                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(tableName).build()).join()
                        .matched();

                DescribeTableResponse tableDescriptionRes = response.response().orElseThrow(
                        () -> new RuntimeException("Customer table was not created."));

                // The actual error can be inspected in response.exception()
                System.out.println(tableDescriptionRes.table().tableName() + " was created.");
            }
        }
    }

    /**
     * Create or update items based on the given list of @SwipeADO
     * Note: the update action here is overwriting the existing items
     *
     * @param items
     */
    public void putOrUpdateItems(DynamoDbAsyncTable<SwipeADO> table, List<SwipeADO> items) {

        // Create a WriteBatch object and add all the WriteRequest objects to it
        WriteBatch.Builder<SwipeADO> writeBatchBuilder = WriteBatch.builder(SwipeADO.class).mappedTableResource(table);

        // Create a list of WriteRequest objects for each item to be written
        items.forEach(writeBatchBuilder::addPutItem);

        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
                BatchWriteItemEnhancedRequest.builder().writeBatches(writeBatchBuilder.build()).build();

        enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }
}

