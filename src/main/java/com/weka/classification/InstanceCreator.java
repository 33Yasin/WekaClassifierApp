package com.weka.classification;

import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import weka.core.Instances;

public class InstanceCreator {
    
    /**
     * GUI bileşenlerinden kullanıcı girdilerini alarak yeni bir instance oluşturur
     * @param dataset Orijinal dataset
     * @param inputComponents GUI bileşenleri (atribut adı -> component)
     * @return Instance değerleri (class atributu hariç)
     */
    public double[] createInstance(Instances dataset, Map<String, JComponent> inputComponents) throws Exception {
        if (dataset == null) {
            throw new Exception("Dataset null olamaz");
        }
        
        if (inputComponents == null || inputComponents.isEmpty()) {
            throw new Exception("Input components boş olamaz");
        }
        
        // Class atributu hariç atribut sayısı
        int numAttributes = dataset.numAttributes() - 1;
        double[] values = new double[numAttributes];
        
        int valueIndex = 0;
        
        // Her atribut için değeri al
        for (int i = 0; i < dataset.numAttributes(); i++) {
            // Class atributunu atla
            if (i == dataset.classIndex()) {
                continue;
            }
            
            weka.core.Attribute attribute = dataset.attribute(i);
            String attributeName = attribute.name();
            
            JComponent component = inputComponents.get(attributeName);
            if (component == null) {
                throw new Exception("Atribut için girdi komponenti bulunamadı: " + attributeName);
            }
            
            double value = extractValueFromComponent(component, attribute);
            values[valueIndex++] = value;
        }
        
        return values;
    }
    
    /**
     * GUI bileşeninden değeri çıkarır ve WEKA formatına dönüştürür
     * @param component GUI bileşeni
     * @param attribute WEKA attribute
     * @return Dönüştürülmüş değer
     */
    private double extractValueFromComponent(JComponent component, weka.core.Attribute attribute) throws Exception {
        if (attribute.isNominal()) {
            // Nominal atribut - ComboBox'tan seçilen değeri al
            if (component instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) component;
                String selectedValue = (String) comboBox.getSelectedItem();
                
                if (selectedValue == null || selectedValue.trim().isEmpty()) {
                    throw new Exception("Nominal atribut için değer seçilmedi: " + attribute.name());
                }
                
                // String değeri index'e dönüştür
                int valueIndex = attribute.indexOfValue(selectedValue);
                if (valueIndex == -1) {
                    throw new Exception("Geçersiz nominal değer: " + selectedValue + 
                        " (Atribut: " + attribute.name() + ")");
                }
                
                return (double) valueIndex;
            } else {
                throw new Exception("Nominal atribut için ComboBox bekleniyor: " + attribute.name());
            }
            
        } else if (attribute.isNumeric()) {
            // Numeric atribut - TextField'dan sayısal değeri al
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                String textValue = textField.getText().trim();
                
                if (textValue.isEmpty()) {
                    throw new Exception("Numeric atribut için değer girilmedi: " + attribute.name());
                }
                
                try {
                    return Double.parseDouble(textValue);
                } catch (NumberFormatException e) {
                    throw new Exception("Geçersiz numeric değer: " + textValue + 
                        " (Atribut: " + attribute.name() + ")");
                }
            } else {
                throw new Exception("Numeric atribut için TextField bekleniyor: " + attribute.name());
            }
            
        } else if (attribute.isString()) {
            // String atribut - TextField'dan string değeri al
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                String textValue = textField.getText().trim();
                
                if (textValue.isEmpty()) {
                    throw new Exception("String atribut için değer girilmedi: " + attribute.name());
                }
                
                // String değeri WEKA'da saklamak için
                return attribute.addStringValue(textValue);
            } else {
                throw new Exception("String atribut için TextField bekleniyor: " + attribute.name());
            }
            
        } else {
            throw new Exception("Desteklenmeyen atribut tipi: " + attribute.name());
        }
    }
    
    /**
     * Verilen instance değerlerini string formatında gösterir (debug için)
     * @param dataset Dataset
     * @param values Instance değerleri
     * @return String gösterimi
     */
    public String instanceToString(Instances dataset, double[] values) {
        if (dataset == null || values == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Instance: [");
        
        int valueIndex = 0;
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (i == dataset.classIndex()) {
                continue; // Class atributunu atla
            }
            
            if (valueIndex > 0) {
                sb.append(", ");
            }
            
            weka.core.Attribute attribute = dataset.attribute(i);
            sb.append(attribute.name()).append("=");
            
            if (valueIndex < values.length) {
                double value = values[valueIndex++];
                
                if (attribute.isNominal()) {
                    sb.append(attribute.value((int) value));
                } else if (attribute.isNumeric()) {
                    sb.append(String.format("%.3f", value));
                } else if (attribute.isString()) {
                    sb.append("\"").append(attribute.value((int) value)).append("\"");
                } else {
                    sb.append(value);
                }
            } else {
                sb.append("?");
            }
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Input componentlerini temizler
     * @param inputComponents GUI bileşenleri
     */
    public void clearInputComponents(Map<String, JComponent> inputComponents) {
        if (inputComponents == null) {
            return;
        }
        
        for (JComponent component : inputComponents.values()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedIndex(0);
            }
        }
    }
    
    /**
     * Input componentlerinin değerlerini validate eder
     * @param dataset Dataset
     * @param inputComponents GUI bileşenleri
     * @return Validation başarılı ise true
     */
    public boolean validateInputs(Instances dataset, Map<String, JComponent> inputComponents) {
        try {
            createInstance(dataset, inputComponents);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Belirtilen atribut için varsayılan değeri ayarlar
     * @param attributeName Atribut adı
     * @param component GUI bileşeni
     * @param attribute WEKA attribute
     */
    public void setDefaultValue(String attributeName, JComponent component, weka.core.Attribute attribute) {
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            if (attribute.isNumeric()) {
                textField.setText("0");
            } else {
                textField.setText("");
            }
        } else if (component instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) component;
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
        }
    }
}