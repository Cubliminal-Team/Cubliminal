package net.limit.cubliminal.world.placement;

import io.github.jdiemke.triangulation.Vector2D;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.world.room.Room;
import net.limit.cubliminal.world.room.RoomRegistry;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import java.util.*;

public record PoissonDiskSampler(int width, int height, int maxSamples) {

    public List<Vec2i> generate(List<Room.Instance> instances, boolean[] roomCache, RegistryEntry<Biome>[] biomeGrid, List<Vec2i> validPositions, Random random) {
        int cells = width * height;
        List<Vec2i> points = new ArrayList<>(cells);
        List<Vec2i> active = new ArrayList<>(cells);

        // Choose 5 random starting points
        int initialPoints = Math.min(5, validPositions.size());
        for (int i = 0; i < initialPoints; i++) {
            active.add(validPositions.get(i));
        }

        int roomSamples = 0;
        final int expectedRooms = Math.min(validPositions.size() / 4, cells / 16);
        int consecutiveFailures = 0;
        final int maxFailures = cells / 8;

        label0:
        while (!active.isEmpty() && roomSamples < expectedRooms && consecutiveFailures < maxFailures) {

            Vec2i activeCenter = active.get(random.nextInt(active.size()));
            RegistryKey<Biome> biome = //biomeGrid.get(activeCenter).getKey().orElseThrow();
                    CubliminalBiomes.DEEP_AQUILA_SECTOR_BIOME;
            boolean hasMapping = RoomRegistry.contains(biome);

            // Use a default room in case the biome doesn't have a mapping
            Room.Instance newInstance = hasMapping ? RoomRegistry.forBiome(biome, random).newInstance(random) : Room.DEFAULT.newInstance(random);
            float spacing = hasMapping ? RoomRegistry.getSpacing(biome) : 1.0f;
            byte roomWidth = newInstance.height();
            byte roomHeight = newInstance.width();
            double minSeparation = this.dynSeparation(roomWidth, roomHeight, spacing, validPositions.size(), expectedRooms - roomSamples);

            for (int i = 0; i < maxSamples; i++) {
                float angle = (float) (random.nextFloat() * Math.PI * 2);
                // Max separation is twice the minimum
                Vector2D dir = (new Vector2D((float) Math.sin(angle), (float) Math.cos(angle))).mult((1 + random.nextFloat()) * minSeparation);
                Vec2i newCenter = activeCenter.add(separate(dir.x), separate(dir.y));
                // Continue if the new room doesn't collide with another
                if (this.isValid(newCenter, roomWidth, roomHeight, roomCache, validPositions, hasMapping)) {
                    // Add it to the generation list if it had a mapping
                    if (hasMapping) {
                        ++roomSamples;
                        consecutiveFailures = 0;
                        instances.add(newInstance);
                        points.add(newCenter);
                        this.placeRoom(newCenter, roomWidth, roomHeight, roomCache);
                    } else ++consecutiveFailures;
                    // Save it for later use to fill gaps
                    active.add(newCenter);
                    continue label0;
                }
            }

            ++consecutiveFailures;
            active.remove(activeCenter);
        }

        return points;
    }

    private double dynSeparation(byte roomWidth, byte roomHeight, float spacing, int availableCells, int remainingRooms) {
        double base = Math.max(roomWidth, roomHeight);
        double density = (double) availableCells / remainingRooms;
        if (base <= 1) {
            return Math.max(3, spacing * density * 0.8);
        }
        return base + spacing * Math.min(1.5, density);
    }

    private int separate(double d) {
        return (int) (d >= 0 ? Math.ceil(d) : Math.floor(d));
    }

    private boolean isValid(Vec2i candidate, byte roomWidth, byte roomHeight, boolean[] grid, List<Vec2i> validPositions, boolean hasMapping) {
        int startX = candidate.x() - 1;
        int endX = candidate.x() + roomWidth + 1;
        int startY = candidate.y() - 1;
        int endY = candidate.y() + roomHeight + 1;

        if (startX < 0 || endX >= width || startY < 0 || endY >= height) return false;

        for (int y = startY; y <= endY; y++) {
            int row = y * width;
            for (int x = startX; x <= endX; x++) {
                if (grid[row + x]) return false;
            }
        }

        if (hasMapping) {
            return validPositions.contains(new Vec2i((startX + endX) / 2, (startY + endY) / 2));
        }
        return true;
    }

    private void placeRoom(Vec2i candidate, byte roomWidth, byte roomHeight, boolean[] grid) {
        for (int row = 0; row < roomWidth; row++) {
            for (int column = 0; column < roomHeight; column++) {
                grid[(column + candidate.y()) * width + row + candidate.x()] = true;
            }
        }
    }

}