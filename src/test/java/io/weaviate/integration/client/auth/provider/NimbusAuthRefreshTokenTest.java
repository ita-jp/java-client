package io.weaviate.integration.client.auth.provider;

import io.weaviate.integration.client.WeaviateVersion;
import java.io.File;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.auth.nimbus.AuthType;
import io.weaviate.client.v1.auth.nimbus.BaseAuth;
import io.weaviate.client.v1.auth.nimbus.NimbusAuth;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.auth.provider.AuthRefreshTokenProvider;
import io.weaviate.client.v1.misc.model.Meta;

public class NimbusAuthRefreshTokenTest {
  private String address;
  private AccessTokenProvider tokenProvider;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-wcs.yaml")
  ).withExposedService("weaviate-auth-wcs_1", 8085, Wait.forHttp("/v1/.well-known/openid-configuration").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-auth-wcs_1", 8085);
    Integer port = compose.getServicePort("weaviate-auth-wcs_1", 8085);
    address = host + ":" + port;
  }

  @Test
  public void testAuthWCS() throws AuthException, InterruptedException {
    class NimbusAuthAuthImpl extends NimbusAuth {
      @Override
      protected AccessTokenProvider getTokenProvider(Config config, BaseAuth.AuthResponse authResponse, List<String> clientScopes,
        String accessToken, long accessTokenLifeTime, String refreshToken, String clientSecret, AuthType authType) {
        // User Password flow
        tokenProvider = new AuthRefreshTokenProvider(config, authResponse, accessToken, 2l, refreshToken);
        return tokenProvider;
      }
    }

    String password = System.getenv("WCS_DUMMY_CI_PW");
    if (StringUtils.isNotBlank(password)) {
      Config config = new Config("http", address);
      String username = "ms_2d0e007e7136de11d5f29fce7a53dae219a51458@existiert.net";
      assertThat(tokenProvider).isNull();
      NimbusAuthAuthImpl nimbusAuth = new NimbusAuthAuthImpl();
      AccessTokenProvider provider = nimbusAuth.getAccessTokenProvider(config, "", username, password, null, AuthType.USER_PASSWORD);
      WeaviateClient client = new WeaviateClient(config, provider);
      assertThat(tokenProvider).isNotNull();
      // get the access token
      String firstBearerAccessTokenHeader = tokenProvider.getAccessToken();
      assertThat(firstBearerAccessTokenHeader).isNotBlank();
      Result<Meta> meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8085");
      assertThat(meta.getResult().getVersion()).isEqualTo(WeaviateVersion.EXPECTED_WEAVIATE_VERSION);
      Thread.sleep(3000l);
      // get the access token after refresh
      String afterRefreshBearerAccessTokenHeader = tokenProvider.getAccessToken();
      assertThat(firstBearerAccessTokenHeader).isNotEqualTo(afterRefreshBearerAccessTokenHeader);
      meta = client.misc().metaGetter().run();
      assertThat(meta).isNotNull();
      assertThat(meta.getError()).isNull();
      assertThat(meta.getResult().getHostname()).isEqualTo("http://[::]:8085");
      assertThat(meta.getResult().getVersion()).isEqualTo(WeaviateVersion.EXPECTED_WEAVIATE_VERSION);
    } else {
      System.out.println("Skipping WCS Refresh Token test, missing WCS_DUMMY_CI_PW");
    }
  }
}
