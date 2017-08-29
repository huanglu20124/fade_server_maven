package com.hl.test;

import java.util.ArrayList;
import java.util.List;

public class ListTest {
	public static void main(String[] args) {
		List<Integer>list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.remove(0);
		list.add(4);
		
		System.out.println(list.get(0)+"---"+list.get(1)+"---"+list.get(2));
		
		list.add(5);
		list.remove(0);
		System.out.println(list.get(0)+"---"+list.get(1)+"---"+list.get(2));
		System.out.println(list.get(2));
		
	}
}
