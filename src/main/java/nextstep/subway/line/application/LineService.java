package nextstep.subway.line.application;

import nextstep.subway.common.exceptions.NotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Section;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;


@Service
@Transactional
public class LineService {
    private StationService stationService;
    private LineRepository lineRepository;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest request) {

        Station upStation = stationService.findById(request.getUpStationId());
        Station downStation = stationService.findById(request.getDownstationId());

        Section section = Section.builder()
                .upStation(upStation)
                .downStation(downStation)
                .distance(request.getDistance())
                .build();

        Line persistLine = lineRepository.save(request.toLine(section));
        return LineResponse.of(persistLine);
    }

    public List<LineResponse> findAll() {
        return lineRepository.findAll().stream()
                .map(LineResponse::of).collect(Collectors.toList());
    }

    public Line findById(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("invalid " + id));
    }

    public void updateLine(Long id, Line newLine) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("invalid "+id));
        line.update(newLine);
    }

    public void deleteLine(Long id) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("invalid "+id));
        lineRepository.delete(line);
    }
}
