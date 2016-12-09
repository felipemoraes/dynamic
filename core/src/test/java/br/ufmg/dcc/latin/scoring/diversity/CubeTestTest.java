package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.metrics.CubeTest;

public class CubeTestTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		CubeTest cubeTest = new CubeTest();
		
		try (BufferedReader br = new BufferedReader(new FileReader("../share/LM.txt"))) {
			String[][] docnos = new String[10][5];
			String line;
			String topic = null;
			for (int k = 0; k < 5; k++) {
				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < 5; j++) {
						line = br.readLine();
						String[] splitedLine = line.split("\t", 4);
						topic = splitedLine[0];
						docnos[i][j] = splitedLine[2];
					}
				}
				
				System.out.println(topic + " " + cubeTest.getCubeTest(10, topic, docnos));
			}

		}
		 catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
