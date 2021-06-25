package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {
    private Station 광교역;
    private Station 강남역;
    @BeforeEach
    public void setup() {
        광교역 = createStation("광교역");
        강남역 = createStation("강남역");
    }
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철_노선_생성_요청

        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";

        ExtractableResponse<Response> response = createLine(createLineRequest(name, color, upStationId, downStationId, distance));

        // then
        // 지하철_노선_생성됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.body().jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.body().jsonPath().getString("color")).isEqualTo(color);
        assertThat(response.body().jsonPath().getString("createdDate")).isNotNull();
        assertThat(response.body().jsonPath().getString("modifiedDate")).isNotNull();
        assertThat(response.body().jsonPath().getList("stations", Station.class)).contains(광교역, 강남역);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {

        // given
        // 지하철_노선_등록되어_있음
        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";
        ExtractableResponse<Response> response = createLine(createLineRequest(name, color, upStationId, downStationId, distance));


        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.body().jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.body().jsonPath().getString("color")).isEqualTo(color);

        // when
        // 지하철_노선_생성_요청
        response = RestAssured
                .given().log().all()
                .body(createLineRequest(name, color, upStationId, downStationId, distance))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all().extract();

        // then
        // 지하철_노선_생성_실패됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("유효하지않은 stationid로 노선 생성")
    @Test
    void createLineInvalidStationId() {
        // when
        // 지하철_노선_생성_요청

        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 3L;
        String distance = "10";

        ExtractableResponse<Response> response = createLine(createLineRequest(name, color, upStationId, downStationId, distance));

        // then
        // 지하철_노선_생성됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        // 지하철_노선_등록되어_있음
        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";
        LineResponse line1 = createLine(createLineRequest(name, color, upStationId, downStationId, distance)).body().jsonPath().getObject("$",LineResponse.class);
        LineResponse line2 = createLine(createLineRequest("2호선", "파랑", upStationId, downStationId, distance)).body().jsonPath().getObject("$",LineResponse.class);
        LineResponse line3 = createLine(createLineRequest("3호선", "초록", upStationId, downStationId, distance)).body().jsonPath().getObject("$",LineResponse.class);
        // when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get("/lines")
                .then().log().all().extract();

        // then
        // 지하철_노선_목록_응답됨
        // 지하철_노선_목록_포함됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<LineResponse> array = response.body().jsonPath().getList(".", LineResponse.class);
        assertThat(array).hasSize(3);
        assertThat(array).contains(line1,line2,line3);
        assertThat(array.get(0).getStations()).contains(강남역,광교역);
        assertThat(array.get(1).getStations()).contains(강남역,광교역);
        assertThat(array.get(2).getStations()).contains(강남역,광교역);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";
        LineResponse line1 = createLine(createLineRequest(name, color, upStationId, downStationId, distance)).body().jsonPath().getObject("$", LineResponse.class);

        // when
        // 지하철_노선_조회_요청
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/lines/1")
                .then().log().all().extract();

        // then
        // 지하철_노선_응답됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body().jsonPath().getLong("id")).isEqualTo(line1.getId());

    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";
        LineResponse line1 = createLine(createLineRequest(name, color, upStationId, downStationId, distance)).body().jsonPath().getObject("$",LineResponse.class);

        // when
        // 지하철_노선_수정_요청
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(createLineRequest("2호선", "초록", upStationId, downStationId, distance))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/1")
                .then().log().all().extract();

        // then
        // 지하철_노선_수정됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        response = RestAssured.given().log().all()
                .when().get("/lines/1")
                .then().log().all().extract();

        assertThat(response.body().jsonPath().getLong("id")).isEqualTo(1L);
        assertThat(response.body().jsonPath().getString("name")).isEqualTo("2호선");
        assertThat(response.body().jsonPath().getString("color")).isEqualTo("초록");

    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        String name = "1호선";
        String color = "빨강";
        Long upStationId = 1L;
        Long downStationId = 2L;
        String distance = "10";
        LineResponse line1 = createLine(createLineRequest(name, color, upStationId, downStationId, distance)).body().jsonPath().getObject("$",LineResponse.class);

        // when
        // 지하철_노선_제거_요청
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/lines/1")
                .then().log().all().extract();
        // then
        // 지하철_노선_삭제됨

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        response = RestAssured.given().log().all()
                .when().get("/lines/1")
                .then().log().all().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    private Station createStation(String name) {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", name);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/stations")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
        return response.body().jsonPath().getObject("$",Station.class);
    }

    private LineRequest createLineRequest(String name, String color, Long upStationId, Long downStationId, String distance) {
        return LineRequest.builder()
                .name(name)
                .color(color)
                .upStationId(upStationId)
                .downStationId(downStationId)
                .distance(distance)
                .build();
    }

    private ExtractableResponse<Response> createLine(LineRequest req) {
        return RestAssured
                .given().log().all()
                .body(req)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all().extract();
    }
}
