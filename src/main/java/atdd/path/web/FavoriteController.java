package atdd.path.web;

import atdd.path.application.FavoriteService;
import atdd.path.application.GraphService;
import atdd.path.application.dto.FavoritePathResponseView;
import atdd.path.application.dto.FavoritePathsResponseView;
import atdd.path.application.dto.FavoriteStationResponseView;
import atdd.path.application.dto.FavoriteStationsResponseView;
import atdd.path.application.resolver.LoginUser;
import atdd.path.domain.FavoritePath;
import atdd.path.domain.FavoriteStation;
import atdd.path.domain.Member;
import atdd.path.domain.Station;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequestMapping("/favorites")
@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final GraphService graphService;

    public FavoriteController(FavoriteService favoriteService, GraphService graphService) {
        this.favoriteService = favoriteService;
        this.graphService = graphService;
    }

    @PostMapping("/stations/{id}")
    public ResponseEntity<FavoriteStationResponseView> createFavoriteStation(@PathVariable("id") Long stationId,
                                                                             @LoginUser Member member) {

        final FavoriteStation savedFavoriteStation = favoriteService.saveForStation(member, stationId);

        return ResponseEntity.created(URI.create("/favorites/"+ savedFavoriteStation.getId()))
                .body(new FavoriteStationResponseView(savedFavoriteStation));
    }

    @GetMapping("/stations")
    public ResponseEntity<FavoriteStationsResponseView> findFavoriteStations(@LoginUser Member member) {
        final List<FavoriteStation> favorites = favoriteService.findForStations(member);
        return ResponseEntity.ok(new FavoriteStationsResponseView(favorites));
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Object> deleteFavoriteStation(@PathVariable("id") Long favoriteId) {
        favoriteService.deleteForStationById(favoriteId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/paths")
    public ResponseEntity<FavoritePathResponseView> createFavoritePath(@RequestParam Long startId,
                                                                          @RequestParam Long endId,
                                                                          @LoginUser Member member) {

        final FavoritePath savedFavoritePath = favoriteService.saveForPath(member, startId, endId);
        final List<Station> stations = graphService.findPath(savedFavoritePath);

        return ResponseEntity.created(URI.create("/favorites/"+ savedFavoritePath.getId()))
                .body(new FavoritePathResponseView(savedFavoritePath, stations));
    }

    @GetMapping("/paths")
    public ResponseEntity<FavoritePathsResponseView> findFavoritePath(@LoginUser Member member) {
        final List<FavoritePath> findFavoritePaths = favoriteService.findForPaths(member);

        final List<FavoritePathResponseView> views = findFavoritePaths.stream()
                .map(path -> new FavoritePathResponseView(path, graphService.findPath(path)))
                .collect(toList());

        return ResponseEntity.ok(new FavoritePathsResponseView(views));
    }

    @DeleteMapping("/paths/{id}")
    public ResponseEntity<Object> deleteFavoritePaths(@PathVariable("id") Long favoriteId) {
        favoriteService.deleteForPathById(favoriteId);
        return ResponseEntity.noContent().build();
    }

}