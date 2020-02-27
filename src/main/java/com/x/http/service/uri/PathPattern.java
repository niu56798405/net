package com.x.http.service.uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 
 */
public class PathPattern {
	
	private PathTemplate template;
	private boolean isRegex;
	private Pattern pattern;
	
	public PathPattern(PathTemplate path) {
		this.template = path;
		this.isRegex = path.regex() != null;
		if(isRegex) pattern = Pattern.compile(template.regex());
	}
	
	public PathMatcher compile(String path) {
		if(isRegex) {
			Matcher matcher = pattern.matcher(path);
			return matcher.find() ? new PathMatcher(true, matcher, template.groups()) : PathMatcher.FALSE;
		}
		return template.mapping().equals(path) ? PathMatcher.TRUE : PathMatcher.FALSE;
	}
	
}
