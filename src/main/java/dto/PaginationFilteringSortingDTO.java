package main.java.dto;

public class PaginationFilteringSortingDTO {

	private int page;
	private boolean sort;
	private boolean filter;
	private String sortBy;
	private String order;
	private String filterBy;
	private String value;
	
	
	public static Builder builder() {
		return new PaginationFilteringSortingDTO().new Builder();
	}
	
	public class Builder {

		public Builder page(int page) {
			PaginationFilteringSortingDTO.this.page = page;
			return this;
		}

		public Builder sort(boolean sort) {
			PaginationFilteringSortingDTO.this.sort = sort;
			return this;
		}

		public Builder filter(boolean filter) {
			PaginationFilteringSortingDTO.this.filter = filter;
			return this;
		}
		
		public Builder sortBy(String sortBy) {
			PaginationFilteringSortingDTO.this.sortBy = sortBy;
			return this;
		}

		public Builder order(String order) {
			PaginationFilteringSortingDTO.this.order = order;
			return this;
		}
		
		public Builder filterBy(String filterBy) {
			PaginationFilteringSortingDTO.this.filterBy = filterBy;
			return this;
		}

		public Builder value(String value) {
			PaginationFilteringSortingDTO.this.value = value;
			return this;
		}

		public PaginationFilteringSortingDTO build() {
			return PaginationFilteringSortingDTO.this;
		}
	}

	public int getPage() {
		return page;
	}

	public boolean isSort() {
		return sort;
	}

	public boolean isFilter() {
		return filter;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getOrder() {
		return order;
	}

	public String getFilterBy() {
		return filterBy;
	}

	public String getValue() {
		return value;
	}
	
	
	
}
