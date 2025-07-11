package nl.theepicblock.immersive_cursedness.objects;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockCache {
	public final static int CHUNK_SIZE = 2;
	public final static int DEFAULT_MAP_SIZE = 16;
	public final static int DEFAULT_HASHMAP_SIZE = 256;
	private final Int2ObjectMap<Int2ObjectMap<Map<BlockPos,BlockState>>> cache = new Int2ObjectOpenHashMap<>(DEFAULT_MAP_SIZE);
	private int size = 0;

	public synchronized BlockState get(BlockPos p) {
		Int2ObjectMap<Map<BlockPos,BlockState>> chunkSlice = cache.get(p.getX() >> CHUNK_SIZE);
		if (chunkSlice == null) return null;

		Map<BlockPos,BlockState> chunk = chunkSlice.get(p.getZ() >> CHUNK_SIZE);
		if (chunk == null) return null;
		return chunk.get(p);
	}

	public synchronized void put(BlockPos p, BlockState t) {
		Int2ObjectMap<Map<BlockPos,BlockState>> chunkSlice = cache.get(p.getX() >> CHUNK_SIZE);
		if (chunkSlice == null) {
			chunkSlice = new Int2ObjectOpenHashMap<>(DEFAULT_MAP_SIZE);
			cache.put(p.getX() >> CHUNK_SIZE, chunkSlice);
		}

		Map<BlockPos,BlockState> chunk = chunkSlice.get(p.getZ() >> CHUNK_SIZE);
		if (chunk == null) {
			chunk = new HashMap<>(DEFAULT_HASHMAP_SIZE);
			chunkSlice.put(p.getZ() >> CHUNK_SIZE, chunk);
		}

		BlockState v = chunk.put(p, t);
		if (v == null) size++;
	}

	public synchronized int size() {
		return size;
	}

	public synchronized void purge(Chunk2IntMap blockPerChunk, List<FlatStandingRectangle> rects, BiConsumer<BlockPos, BlockState> onRemove) {
		if (size == blockPerChunk.getTotal()) return;

		var sliceIterator = cache.int2ObjectEntrySet().iterator();
		while (sliceIterator.hasNext()) {
			var sliceEntry = sliceIterator.next();
			int x = sliceEntry.getIntKey();
			Int2ObjectMap<Map<BlockPos, BlockState>> cacheSlice = sliceEntry.getValue();

			Int2IntMap countSlice = blockPerChunk.getSlice(x);
			if (countSlice == null) { //there was nothing sent in this entire slice, so it can be purged
				purge(cacheSlice, onRemove);
				sliceIterator.remove();
				continue;
			}

			var mapIterator = cacheSlice.int2ObjectEntrySet().iterator();
			while (mapIterator.hasNext()) {
				var mapEntry = mapIterator.next();
				int zPos = mapEntry.getIntKey();
				int oldZ = mapEntry.getValue().size();
				int newZ = countSlice.get(zPos);

				if (newZ == 0) { //there was nothing sent in this chunk, so it can be purged entirely
					purge(mapEntry.getValue(), onRemove);
					mapIterator.remove();
				} else if (newZ != oldZ) {
					Map<BlockPos, BlockState> map = mapEntry.getValue();
					map.entrySet().removeIf((entry) -> {
						BlockPos mapBlockPos = entry.getKey();
						for (FlatStandingRectangle rect : rects) {
							if (rect.contains(mapBlockPos)) {
								return false;
							}
						}
						onRemove.accept(mapBlockPos, entry.getValue());
						size--;
						return true;
					});
				}
			}
		}
	}

	private void purge(Int2ObjectMap<Map<BlockPos,BlockState>> v, BiConsumer<BlockPos, BlockState> onRemove) {
		v.values().forEach((map) -> purge(map, onRemove));
	}

	private void purge(Map<BlockPos,BlockState> v, BiConsumer<BlockPos, BlockState> onRemove) {
		size -= v.size();
		v.forEach(onRemove);
	}

	public synchronized void purgeAll(BiConsumer<BlockPos, BlockState> onRemove) {
		cache.values().forEach((slice) -> purge(slice, onRemove));
		cache.clear();
		size=0;
	}
}