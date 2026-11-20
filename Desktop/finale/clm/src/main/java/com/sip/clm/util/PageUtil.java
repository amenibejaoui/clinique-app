package com.sip.clm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtil {

	private PageUtil() {
	}

	/** Pagination (page commence a 0), tri par id decroissant (les plus recents d'abord). */
	public static Pageable of(int page, int size) {
		int p = Math.max(page, 0);
		int s = Math.min(Math.max(size, 1), 100);
		return PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
	}
}
