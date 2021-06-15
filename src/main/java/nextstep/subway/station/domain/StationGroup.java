package nextstep.subway.station.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Embeddable;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Embeddable
public class StationGroup {

	@OneToMany
	@JoinColumn(name = "line_id", foreignKey = @ForeignKey(name = "fk_station_line"))
	private List<Station> stations = new ArrayList<>();

	public StationGroup() {
	}

	public StationGroup(List<Station> stations) {
		this.stations = stations.stream().collect(Collectors.toList());
	}

	public List<Station> stations() {
		return stations;
	}

	public int size() {
		return stations.size();
	}

	public boolean contains(Station station) {
		return stations.contains(station);
	}

	public void add(Station station) {
		if (!contains(station)) {
			stations.add(station);
		}
	}

	public void addStationGroup(StationGroup stationGroup) {
		stationGroup.stations.forEach(this::add);
	}

	public void remove(Station station) {
		stations.remove(station);
	}

	public void removeStationGroup(StationGroup stationGroup) {
		stationGroup.stations.forEach(this::remove);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		StationGroup that = (StationGroup)object;
		return stations.containsAll(that.stations)
			&& that.stations.containsAll(stations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stations);
	}
}