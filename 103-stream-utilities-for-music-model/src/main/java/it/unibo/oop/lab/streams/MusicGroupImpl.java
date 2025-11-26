package it.unibo.oop.lab.streams;

import static java.util.stream.Collectors.summingDouble;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return songs.stream()
                    .map(Song::getSongName)
                    .sorted();
    }

    @Override
    public Stream<String> albumNames() {
        return albums.keySet().stream();
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return albums.entrySet().stream()
                     .filter(e -> e.getValue() == year)
                     .map(e -> e.getKey());
    }

    @Override
    public int countSongs(final String albumName) {
        return (int) songs.stream()
                                .filter(songs -> songs.getAlbumName().equals(Optional.of(albumName)))
                                .count();
    }

    @Override
    public int countSongsInNoAlbum() {
        return (int) songs.stream()
                                .filter(songs -> songs.getAlbumName().equals(Optional.empty()))
                                .count();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        return songs.stream()
                    .filter(songs -> songs.getAlbumName().equals(Optional.of(albumName)))
                    .mapToDouble(Song::getDuration)
                    .average();
    }

    @Override
    public Optional<String> longestSong() {
        return songs.stream()
                    .max((a, b) -> Double.compare(a.getDuration(), b.getDuration()))
                    .map(Song::getSongName);
    }

    @Override
    public Optional<String> longestAlbum() {
        return songs.stream()
                    .filter(a -> a.getAlbumName().isPresent())
                    .collect(Collectors.groupingBy(
                        song -> song.getAlbumName().get(),
                        Collectors.summingDouble(Song::getDuration)
                    ))
                    .entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey);
    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
