package test.predict;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.ieee.odm.common.ODMException;
import org.interpss.service.AclfTrainDataGenerator;
import org.junit.Test;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import com.interpss.common.exp.IpssCacheException;

/**
* @Author Donghao Feng
*/
public class ModelPredictTest {
	@Test
	public void test() throws IOException, InterruptedException, IpssCacheException, ODMException, ExecutionException {
		AclfTrainDataGenerator gateway = new AclfTrainDataGenerator();
		//read case
		String filename = "testdata/cases/ieee14.ieee";
		gateway.loadCase(filename, "BusVoltLoadChangeTrainCaseBuilder");
		//run loadflow
		gateway.trainCaseBuilder.createTestCase();
		//generate input
		double[] inputs = gateway.trainCaseBuilder.getNetInput();
		float[][] inputs_f  = new float[1][inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			inputs_f[0][i] =(float) inputs[i];
		}
		//read model
		SavedModelBundle bundle = SavedModelBundle.load("py/c_graph/single_net/model", "voltage");
		//predict
		float[][] output = bundle.session().runner().feed("x", Tensor.create(inputs_f)).fetch("z").run().get(0)
				.copyTo(new float[1][28]);
		double[][] output_d = new double[1][inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			output_d[0][i] = output[0][i];
		}
		//print out mismatch 
		System.out.println("Model out mismatch: "+gateway.getMismatchInfo(output_d[0]));
	}
}
