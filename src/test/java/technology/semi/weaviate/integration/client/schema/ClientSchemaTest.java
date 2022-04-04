package technology.semi.weaviate.integration.client.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.v1.misc.model.BM25Config;
import technology.semi.weaviate.client.v1.misc.model.InvertedIndexConfig;
import technology.semi.weaviate.client.v1.misc.model.ShardingConfig;
import technology.semi.weaviate.client.v1.misc.model.StopwordConfig;
import technology.semi.weaviate.client.v1.misc.model.VectorIndexConfig;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.client.v1.schema.model.Schema;
import technology.semi.weaviate.client.v1.schema.model.Tokenization;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class ClientSchemaTest {
  private String address;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
          new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    address = host + ":" + port;
  }

  @Test
  public void testSchemaCreateBandClass() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schema = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().classDeleter().withClassName(clazz.getClassName()).run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schema);
    assertNotNull(schema.getResult());
    assertEquals(1, schema.getResult().getClasses().size());
    assertEquals(clazz.getClassName(), schema.getResult().getClasses().get(0).getClassName());
    assertEquals(clazz.getDescription(), schema.getResult().getClasses().get(0).getDescription());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
  }

  @Test
  public void testSchemaCreateRunClass() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Run")
            .description("Running from the fuzz")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertEquals(1, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(clazz.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(clazz.getDescription(), schemaAfterCreate.getResult().getClasses().get(0).getDescription());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaDeleteClasses() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deletePizzaStatus = client.schema().classDeleter().withClassName(pizza.getClassName()).run();
    Result<Boolean> deleteChickenSoupStatus = client.schema().classDeleter().withClassName(chickenSoup.getClassName()).run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    assertNotNull(chickenSoupCreateStatus);
    assertTrue(chickenSoupCreateStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(pizza.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getResult().getClasses().get(1).getDescription());
    assertNotNull(deletePizzaStatus);
    assertTrue(deletePizzaStatus.getResult());
    assertNotNull(deleteChickenSoupStatus);
    assertTrue(deleteChickenSoupStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaDeleteAllSchema() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();
    // then
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    assertNotNull(chickenSoupCreateStatus);
    assertTrue(chickenSoupCreateStatus.getResult());
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(2, schemaAfterCreate.getResult().getClasses().size());
    assertEquals(pizza.getClassName(), schemaAfterCreate.getResult().getClasses().get(0).getClassName());
    assertEquals(chickenSoup.getDescription(), schemaAfterCreate.getResult().getClasses().get(1).getDescription());
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
    assertEquals(0, schemaAfterDelete.getResult().getClasses().size());
  }

  @Test
  public void testSchemaCreateClassesAddProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    WeaviateClass chickenSoup = WeaviateClass.builder()
            .className("ChickenSoup")
            .description("A soup made in part out of chicken, not for chicken.")
            .build();
    Property newProperty = Property.builder()
            .dataType(Arrays.asList(DataType.STRING))
            .description("name")
            .name("name")
            .build();
    // when
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> chickenSoupCreateStatus = client.schema().classCreator().withClass(chickenSoup).run();
    Result<Boolean> pizzaPropertyCreateStatus = client.schema().propertyCreator()
            .withProperty(newProperty).withClassName(pizza.getClassName()).run();
    Result<Boolean> chickenSoupPropertyCreateStatus = client.schema().propertyCreator()
            .withProperty(newProperty).withClassName(chickenSoup.getClassName()).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

    // then
    assertResultTrue(pizzaCreateStatus);
    assertResultTrue(chickenSoupCreateStatus);
    assertResultTrue(pizzaPropertyCreateStatus);
    assertResultTrue(chickenSoupPropertyCreateStatus);
    assertClassesSize(2, schemaAfterCreate);

    WeaviateClass resultPizzaClass = schemaAfterCreate.getResult().getClasses().get(0);
    assertClassEquals(pizza.getClassName(), pizza.getDescription(), resultPizzaClass);
    assertPropertiesSize(1, resultPizzaClass);
    assertPropertyEquals(newProperty.getName(), "word", resultPizzaClass.getProperties().get(0));

    WeaviateClass resultChickenSoupClass = schemaAfterCreate.getResult().getClasses().get(1);
    assertClassEquals(chickenSoup.getClassName(), chickenSoup.getDescription(), resultChickenSoupClass);
    assertPropertiesSize(1, resultChickenSoupClass);
    assertPropertyEquals(newProperty.getName(), "word", resultChickenSoupClass.getProperties().get(0));

    assertResultTrue(deleteAllStatus);
    assertClassesSize(0, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Article")
            .description("A written text, for example a news article or blog post")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .properties(new ArrayList() {{
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.STRING);
                      }})
                      .description("Title of the article")
                      .name("title")
                      .tokenization(Tokenization.FIELD)
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.TEXT);
                      }})
                      .description("The content of the article")
                      .name("content")
                      .tokenization(Tokenization.WORD)
                      .build());
            }})
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

    // then
    assertResultTrue(createStatus);
    assertClassesSize(1, schemaAfterCreate);

    WeaviateClass resultArticleClass = schemaAfterCreate.getResult().getClasses().get(0);
    assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArticleClass);
    assertPropertiesSize(2, resultArticleClass);
    assertPropertyEquals("title", "field", resultArticleClass.getProperties().get(0));
    assertPropertyEquals("content", "word", resultArticleClass.getProperties().get(1));

    assertResultTrue(deleteStatus);
    assertClassesSize(0, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassExplicitVectorizerWithArrayProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("ClassArrays")
            .description("Class which properties are all array properties")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .properties(new ArrayList() {{
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.STRING_ARRAY);
                      }})
                      .name("stringArray")
                      .tokenization(Tokenization.FIELD)
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.TEXT_ARRAY);
                      }})
                      .name("textArray")
                      .tokenization(Tokenization.WORD)
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.INT_ARRAY);
                      }})
                      .name("intArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.NUMBER_ARRAY);
                      }})
                      .name("numberArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.BOOLEAN_ARRAY);
                      }})
                      .name("booleanArray")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.DATE_ARRAY);
                      }})
                      .name("dateArray")
                      .build());
            }})
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

    // then
    assertResultTrue(createStatus);
    assertClassesSize(1, schemaAfterCreate);

    WeaviateClass resultArraysClass = schemaAfterCreate.getResult().getClasses().get(0);
    assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArraysClass);
    assertPropertiesSize(6, resultArraysClass);
    assertPropertyEquals("stringArray", DataType.STRING_ARRAY, "field", resultArraysClass.getProperties().get(0));
    assertPropertyEquals("textArray", DataType.TEXT_ARRAY, "word", resultArraysClass.getProperties().get(1));
    assertPropertyEquals("intArray", DataType.INT_ARRAY, null, resultArraysClass.getProperties().get(2));
    assertPropertyEquals("numberArray", DataType.NUMBER_ARRAY, null, resultArraysClass.getProperties().get(3));
    assertPropertyEquals("booleanArray", DataType.BOOLEAN_ARRAY, null, resultArraysClass.getProperties().get(4));
    assertPropertyEquals("dateArray", DataType.DATE_ARRAY, null, resultArraysClass.getProperties().get(5));

    assertResultTrue(deleteStatus);
    assertClassesSize(0, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassWithProperties() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Article")
            .description("A written text, for example a news article or blog post")
            .properties(new ArrayList() {{
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.STRING);
                      }})
                      .description("Title of the article")
                      .name("title")
                      .build());
              add(Property.builder()
                      .dataType(new ArrayList() {{
                        add(DataType.TEXT);
                      }})
                      .description("The content of the article")
                      .name("content")
                      .build());
            }})
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<Schema> schemaAfterCreate = client.schema().getter().run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();
    Result<Schema> schemaAfterDelete = client.schema().getter().run();

    // then
    assertResultTrue(createStatus);
    assertClassesSize(1, schemaAfterCreate);

    WeaviateClass resultArticleClass = schemaAfterCreate.getResult().getClasses().get(0);
    assertClassEquals(clazz.getClassName(), clazz.getDescription(), resultArticleClass);
    assertPropertiesSize(2, resultArticleClass);
    assertPropertyEquals("title", "word", resultArticleClass.getProperties().get(0));
    assertPropertyEquals("content", "word", resultArticleClass.getProperties().get(1));

    assertResultTrue(deleteStatus);
    assertClassesSize(0, schemaAfterDelete);
  }

  @Test
  public void testSchemaCreateClassWithInvalidTokenizationProperty() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass pizza = WeaviateClass.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();

    Property notExistingTokenization = Property.builder()
            .dataType(Collections.singletonList(DataType.STRING))
            .description("someString")
            .name("someString")
            .tokenization("not-existing")
            .build();
    Property notSupportedTokenizationForText = Property.builder()
            .dataType(Collections.singletonList(DataType.TEXT))
            .description("someText")
            .name("someText")
            .tokenization(Tokenization.FIELD)
            .build();
    Property notSupportedTokenizationForInt = Property.builder()
            .dataType(Collections.singletonList(DataType.INT))
            .description("someInt")
            .name("someInt")
            .tokenization(Tokenization.WORD)
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(pizza).run();
    Result<Boolean> notExistingTokenizationCreateStatus = client.schema().propertyCreator()
            .withProperty(notExistingTokenization).withClassName(pizza.getClassName()).run();
    Result<Boolean> notSupportedTokenizationForTextCreateStatus = client.schema().propertyCreator()
            .withProperty(notSupportedTokenizationForText).withClassName(pizza.getClassName()).run();
    Result<Boolean> notSupportedTokenizationForIntCreateStatus = client.schema().propertyCreator()
            .withProperty(notSupportedTokenizationForInt).withClassName(pizza.getClassName()).run();
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();

    //then
    assertResultTrue(createStatus);

    assertResultError("tokenization in body should be one of [word field]", notExistingTokenizationCreateStatus);
    assertResultError("Tokenization 'field' is not allowed for data type 'text'", notSupportedTokenizationForTextCreateStatus);
    assertResultError("Tokenization 'word' is not allowed for data type 'int'", notSupportedTokenizationForIntCreateStatus);

    assertResultTrue(deleteAllStatus);
  }

  @Test
  public void testCreateClassWithBM25Config() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);

    BM25Config bm25Config = BM25Config.builder()
            .b(0.777f)
            .k1(1.777f)
            .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
            .bm25(bm25Config)
            .build();

    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .invertedIndexConfig(invertedIndexConfig)
            .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getBm25());
    assertEquals(bm25Config.getB(), bandClass.getResult().getInvertedIndexConfig().getBm25().getB());
    assertEquals(bm25Config.getK1(), bandClass.getResult().getInvertedIndexConfig().getBm25().getK1());
    assertResultTrue(deleteStatus);
  }

  @Test
  public void testCreateClassWithStopwordsConfig() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);

    StopwordConfig stopwordConfig = StopwordConfig.builder()
            .preset("en")
            .additions(new String[]{ "star", "nebula" })
            .removals(new String[]{ "a", "the" })
            .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
            .stopwords(stopwordConfig)
            .build();

    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .invertedIndexConfig(invertedIndexConfig)
            .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getStopwords());
    assertEquals(stopwordConfig.getPreset(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getRemovals());
    assertResultTrue(deleteStatus);
  }

  @Test
  public void testCreateClassWithBM25ConfigAndWithStopwordsConfig() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);

    BM25Config bm25Config = BM25Config.builder()
            .b((float) 0.777)
            .k1((float) 1.777)
            .build();

    StopwordConfig stopwordConfig = StopwordConfig.builder()
            .preset("en")
            .additions(new String[]{ "star", "nebula" })
            .removals(new String[]{ "a", "the" })
            .build();

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
            .bm25(bm25Config)
            .stopwords(stopwordConfig)
            .build();

    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .invertedIndexConfig(invertedIndexConfig)
            .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getBm25());
    assertEquals(bm25Config.getB(), bandClass.getResult().getInvertedIndexConfig().getBm25().getB());
    assertEquals(bm25Config.getK1(), bandClass.getResult().getInvertedIndexConfig().getBm25().getK1());
    assertNotNull(bandClass.getResult().getInvertedIndexConfig().getStopwords());
    assertEquals(stopwordConfig.getPreset(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), bandClass.getResult().getInvertedIndexConfig().getStopwords().getRemovals());
    assertResultTrue(deleteStatus);
  }

  @Test
  public void testCreateClassWithInvertedIndexConfigAndVectorIndexConfigAndShardConfig() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    // inverted index config
    BM25Config bm25Config = BM25Config.builder()
            .b((float) 0.777)
            .k1((float) 1.777)
            .build();
    StopwordConfig stopwordConfig = StopwordConfig.builder()
            .preset("en")
            .additions(new String[]{ "star", "nebula" })
            .removals(new String[]{ "a", "the" })
            .build();
    Integer cleanupIntervalSeconds = 300;
    // vector index config
    Integer efConstruction = 128;
    Integer maxConnections = 64;
    Integer vectorCacheMaxObjects = 500000;
    Integer ef = -1;
    Boolean skip = false;
    Integer dynamicEfFactor = 8;
    Integer dynamicEfMax = 500;
    Integer dynamicEfMin = 100;
    Integer flatSearchCutoff = 40000;
    // shard config
    Integer actualCount = 1;
    Integer actualVirtualCount = 128;
    Integer desiredCount = 1;
    Integer desiredVirtualCount = 128;
    String function = "murmur3";
    String key = "_id";
    String strategy = "hash";
    Integer virtualPerPhysical = 128;

    InvertedIndexConfig invertedIndexConfig = InvertedIndexConfig.builder()
            .bm25(bm25Config)
            .stopwords(stopwordConfig)
            .cleanupIntervalSeconds(cleanupIntervalSeconds)
            .build();

    VectorIndexConfig vectorIndexConfig = VectorIndexConfig.builder()
            .cleanupIntervalSeconds(cleanupIntervalSeconds)
            .efConstruction(efConstruction)
            .maxConnections(maxConnections)
            .vectorCacheMaxObjects(vectorCacheMaxObjects)
            .ef(ef)
            .skip(skip)
            .dynamicEfFactor(dynamicEfFactor)
            .dynamicEfMax(dynamicEfMax)
            .dynamicEfMin(dynamicEfMin)
            .flatSearchCutoff(flatSearchCutoff)
            .build();

    ShardingConfig shardingConfig = ShardingConfig.builder()
            .actualCount(actualCount)
            .actualVirtualCount(actualVirtualCount)
            .desiredCount(desiredCount)
            .desiredVirtualCount(desiredVirtualCount)
            .function(function)
            .key(key)
            .strategy(strategy)
            .virtualPerPhysical(virtualPerPhysical)
            .build();

    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .invertedIndexConfig(invertedIndexConfig)
            .vectorIndexConfig(vectorIndexConfig)
            .shardingConfig(shardingConfig)
            .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<Boolean> deleteStatus = client.schema().allDeleter().run();

    // then
    assertNotNull(createStatus);
    assertNull(createStatus.getError());
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    InvertedIndexConfig classInvertedIndexConfig = bandClass.getResult().getInvertedIndexConfig();
    assertNotNull(classInvertedIndexConfig.getBm25());
    assertEquals(bm25Config.getB(), classInvertedIndexConfig.getBm25().getB());
    assertEquals(bm25Config.getK1(), classInvertedIndexConfig.getBm25().getK1());
    assertNotNull(classInvertedIndexConfig.getStopwords());
    assertEquals(stopwordConfig.getPreset(), classInvertedIndexConfig.getStopwords().getPreset());
    assertArrayEquals(stopwordConfig.getAdditions(), classInvertedIndexConfig.getStopwords().getAdditions());
    assertArrayEquals(stopwordConfig.getRemovals(), classInvertedIndexConfig.getStopwords().getRemovals());
    assertEquals(cleanupIntervalSeconds, classInvertedIndexConfig.getCleanupIntervalSeconds());
    VectorIndexConfig classVectorIndexConfig = bandClass.getResult().getVectorIndexConfig();
    assertEquals(maxConnections, classVectorIndexConfig.getMaxConnections());
    assertEquals(efConstruction, classVectorIndexConfig.getEfConstruction());
    assertEquals(vectorCacheMaxObjects, classVectorIndexConfig.getVectorCacheMaxObjects());
    assertEquals(ef, classVectorIndexConfig.getEf());
    assertEquals(skip, classVectorIndexConfig.getSkip());
    assertEquals(dynamicEfFactor, classVectorIndexConfig.getDynamicEfFactor());
    assertEquals(dynamicEfMax, classVectorIndexConfig.getDynamicEfMax());
    assertEquals(dynamicEfMin, classVectorIndexConfig.getDynamicEfMin());
    assertEquals(flatSearchCutoff, classVectorIndexConfig.getFlatSearchCutoff());
    ShardingConfig classShardingIndexConfig = bandClass.getResult().getShardingConfig();
    assertEquals(actualCount, classShardingIndexConfig.getActualCount());
    assertEquals(actualVirtualCount, classShardingIndexConfig.getActualVirtualCount());
    assertEquals(desiredCount, classShardingIndexConfig.getDesiredCount());
    assertEquals(desiredVirtualCount, classShardingIndexConfig.getDesiredVirtualCount());
    assertEquals(function, classShardingIndexConfig.getFunction());
    assertEquals(key, classShardingIndexConfig.getKey());
    assertEquals(strategy, classShardingIndexConfig.getStrategy());
    assertEquals(virtualPerPhysical, classShardingIndexConfig.getVirtualPerPhysical());
    assertResultTrue(deleteStatus);
  }

  @Test
  public void testSchemaGetBandClass() {
    // given
    Config config = new Config("http", address);
    WeaviateClient client = new WeaviateClient(config);
    WeaviateClass clazz = WeaviateClass.builder()
            .className("Band")
            .description("Band that plays and produces music")
            .vectorIndexType("hnsw")
            .vectorizer("text2vec-contextionary")
            .build();
    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    Result<WeaviateClass> bandClass = client.schema().classGetter().withClassName(clazz.getClassName()).run();
    Result<WeaviateClass> nonExistentClass = client.schema().classGetter().withClassName("nonExistentClass").run();
    Result<Boolean> deleteStatus = client.schema().classDeleter().withClassName(clazz.getClassName()).run();
    // then
    assertNotNull(createStatus);
    assertTrue(createStatus.getResult());
    assertNotNull(bandClass);
    assertNotNull(bandClass.getResult());
    assertNull(bandClass.getError());
    assertEquals(clazz.getClassName(), bandClass.getResult().getClassName());
    assertEquals(clazz.getDescription(), bandClass.getResult().getDescription());
    assertEquals(clazz.getVectorIndexType(), bandClass.getResult().getVectorIndexType());
    assertEquals(clazz.getVectorizer(), bandClass.getResult().getVectorizer());
    assertNotNull(nonExistentClass);
    assertNull(nonExistentClass.getError());
    assertNull(nonExistentClass.getResult());
    assertNotNull(deleteStatus);
    assertTrue(deleteStatus.getResult());
  }

  private void assertResultTrue(Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.getResult());
  }

  private void assertResultError(String msg, Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.hasErrors());
    List<WeaviateErrorMessage> messages = result.getError().getMessages();
    assertEquals(1, messages.size());
    assertEquals(msg, messages.get(0).getMessage());
  }

  private void assertClassesSize(int expectedSize, Result<Schema> schemaAfterCreate) {
    assertNotNull(schemaAfterCreate);
    assertNotNull(schemaAfterCreate.getResult());
    assertNotNull(schemaAfterCreate.getResult().getClasses());
    assertEquals(expectedSize, schemaAfterCreate.getResult().getClasses().size());
  }

  private void assertClassEquals(String expectedName, String expectedDescription, WeaviateClass schemaClass) {
    assertEquals(expectedName, schemaClass.getClassName());
    assertEquals(expectedDescription, schemaClass.getDescription());
  }

  private void assertPropertiesSize(int expectedSize, WeaviateClass schemaClass) {
    assertNotNull(schemaClass.getProperties());
    assertEquals(expectedSize, schemaClass.getProperties().size());
  }

  private void assertPropertyEquals(String expectedName, String expectedTokenization, Property property) {
    assertEquals(expectedName, property.getName());
    assertEquals(expectedTokenization, property.getTokenization());
  }

  private void assertPropertyEquals(String expectedName, String expectedDataType, String expectedTokenization, Property property) {
    assertPropertyEquals(expectedName, expectedTokenization, property);
    assertTrue(property.getDataType().size() > 0);
    assertEquals(expectedDataType, property.getDataType().get(0));
  }
}
