package me.vinceh121.jskolengo.pagination;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.github.jasminb.jsonapi.JSONAPIDocument;

public class JSONAPIPaginatedCollection<T> implements Iterable<T> {
	private final PageFetcher<JSONAPIDocument<List<T>>> fetcher;
	private int batchSize = 10;

	public JSONAPIPaginatedCollection(PageFetcher<JSONAPIDocument<List<T>>> fetcher) {
		this.fetcher = fetcher;
	}

	@Override
	public Iterator<T> iterator() {
		return new PaginatedIterator();
	}

	private class PaginatedIterator implements Iterator<T> {
		private JSONAPIDocument<List<T>> currentCollection;
		/**
		 * Index within currentCollection
		 */
		private int currentLocalIndex;
		private int currentPage;

		public PaginatedIterator() {
			try {
				this.currentCollection = fetcher.fetchPage(batchSize, 0);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			return this.currentCollection.getLinks().getNext() != null;
		}

		@Override
		public T next() {
			try {
				if (currentLocalIndex < currentCollection.get().size()) {
					T next = currentCollection.get().get(currentLocalIndex);
					currentLocalIndex++;
					return next;
				} else {
					currentPage++;
					currentCollection = fetcher.fetchPage(batchSize, currentPage * batchSize);
					currentLocalIndex = 1; // prevent duplicates
					return currentCollection.get().get(0);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
