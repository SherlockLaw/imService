package com.sherlock.imService.utils;

public class ListUtil {

	private int id;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	@FunctionalInterface
	interface Action<T> {
		public void execute(T t);
	}
	public static <T> void test(Action<T> action,T Str){
		action.execute(Str);
	}
	public static <T> void encho(T Str) {
		System.out.println(Str);
	}
	
	
	public static void main(String[] args) {
		test(ListUtil::encho,"sdf");
	}
}
