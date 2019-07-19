package com.mikechristoff.lambda;

public class DoMethodUsage {
	
	public static void main(String args[]) {
		DoMethod d = (
			i -> {
				System.out.println(i);
				return i;
			});
		
		d.doTask(10);
		
		////
		
		((DoMethod)
			(i -> {
				System.out.println(i);
				return i;
			}
		)).doTask(10);	
	}

}
