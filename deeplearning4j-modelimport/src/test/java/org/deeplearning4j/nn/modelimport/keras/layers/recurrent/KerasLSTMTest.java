/*-
 *
 *  * Copyright 2017 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package org.deeplearning4j.nn.modelimport.keras.layers.recurrent;

import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.modelimport.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.config.Keras2LayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.config.KerasLayerConfiguration;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Max Pumperla
 */
public class KerasLSTMTest {

    private final String ACTIVATION_KERAS = "linear";
    private final String ACTIVATION_DL4J = "identity";
    private final String LAYER_NAME = "lstm_layer";
    private final String INIT_KERAS = "glorot_normal";
    private final WeightInit INIT_DL4J = WeightInit.XAVIER;
    private final double L1_REGULARIZATION = 0.01;
    private final double L2_REGULARIZATION = 0.02;
    private final double DROPOUT_KERAS = 0.3;
    private final double DROPOUT_DL4J = 1 - DROPOUT_KERAS;
    private final int N_OUT = 13;


    private Integer keras1 = 1;
    private Integer keras2 = 2;
    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();
    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    public void testLstmLayer() throws Exception {
        buildLstmLayer(conf1, keras1);
        buildLstmLayer(conf2, keras2);
    }

    void buildLstmLayer(KerasLayerConfiguration conf, Integer kerasVersion) throws Exception {
        String innerActivation = "hard_sigmoid";
        double lstmForgetBiasDouble = 1.0;
        String lstmForgetBiasString = "one";
        boolean lstmUnroll = true;

        KerasLstm lstm = new KerasLstm(kerasVersion);

        Map<String, Object> layerConfig = new HashMap<String, Object>();
        layerConfig.put(conf.getLAYER_FIELD_CLASS_NAME(), conf.getLAYER_CLASS_NAME_LSTM());
        Map<String, Object> config = new HashMap<String, Object>();
        config.put(conf.getLAYER_FIELD_ACTIVATION(), ACTIVATION_KERAS); // keras linear -> dl4j identity
        config.put(conf.getLAYER_FIELD_INNER_ACTIVATION(), innerActivation); // keras linear -> dl4j identity
        config.put(conf.getLAYER_FIELD_NAME(), LAYER_NAME);
        if (kerasVersion == 1) {
            config.put(conf.getLAYER_FIELD_INNER_INIT(), INIT_KERAS);
            config.put(conf.getLAYER_FIELD_INIT(), INIT_KERAS);

        } else {
            Map<String, Object> init = new HashMap<String, Object>();
            init.put("class_name", conf.getINIT_GLOROT_NORMAL());
            config.put(conf.getLAYER_FIELD_INNER_INIT(), init);
            config.put(conf.getLAYER_FIELD_INIT(), init);
        }
        Map<String, Object> W_reg = new HashMap<String, Object>();
        W_reg.put(conf.getREGULARIZATION_TYPE_L1(), L1_REGULARIZATION);
        W_reg.put(conf.getREGULARIZATION_TYPE_L2(), L2_REGULARIZATION);
        config.put(conf.getLAYER_FIELD_W_REGULARIZER(), W_reg);
        config.put(conf.getLAYER_FIELD_RETURN_SEQUENCES(), true);

        config.put(conf.getLAYER_FIELD_DROPOUT_W(), DROPOUT_KERAS);
        config.put(conf.getLAYER_FIELD_DROPOUT_U(), 0.0);
        config.put(conf.getLAYER_FIELD_FORGET_BIAS_INIT(), lstmForgetBiasString);
        config.put(conf.getLAYER_FIELD_OUTPUT_DIM(), N_OUT);
        config.put(lstm.getLAYER_FIELD_UNROLL(), lstmUnroll);
        layerConfig.put(conf.getLAYER_FIELD_CONFIG(), config);
        layerConfig.put(conf.getLAYER_FIELD_KERAS_VERSION(), kerasVersion);

        LSTM layer = new KerasLstm(layerConfig).getLSTMLayer();
        assertEquals(ACTIVATION_DL4J, layer.getActivationFn().toString());
        assertEquals(LAYER_NAME, layer.getLayerName());
        assertEquals(INIT_DL4J, layer.getWeightInit());
        assertEquals(L1_REGULARIZATION, layer.getL1(), 0.0);
        assertEquals(L2_REGULARIZATION, layer.getL2(), 0.0);
        assertEquals(DROPOUT_DL4J, layer.getDropOut(), 0.0);
        assertEquals(lstmForgetBiasDouble, layer.getForgetGateBiasInit(), 0.0);
        assertEquals(N_OUT, layer.getNOut());
    }
}
