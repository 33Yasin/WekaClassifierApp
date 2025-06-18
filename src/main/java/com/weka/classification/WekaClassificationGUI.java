package com.weka.classification;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class WekaClassificationGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JLabel datasetLabel;
    private JButton selectDatasetButton;
    private JTextArea resultsArea;
    private JPanel inputPanel;
    private JButton discoverButton;
    private JLabel bestAlgorithmLabel;
    
    private Instances dataset;
    private String bestAlgorithm;
    private double bestAccuracy;
    private Map<String, JComponent> inputComponents;
    private ClassificationEngine classificationEngine;
    private InstanceCreator instanceCreator;
    
    public WekaClassificationGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        classificationEngine = new ClassificationEngine();
        instanceCreator = new InstanceCreator();
        inputComponents = new HashMap<>();
    }
    
    private void initializeComponents() {
        setTitle("WEKA Classification Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        datasetLabel = new JLabel("Dataset: Henüz seçilmedi");
        selectDatasetButton = new JButton("Dataset Seç");
        resultsArea = new JTextArea(10, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        inputPanel = new JPanel();
        inputPanel.setBorder(new TitledBorder("Yeni Instance Girdileri"));
        inputPanel.setLayout(new GridBagLayout());
        
        discoverButton = new JButton("Keşfet");
        discoverButton.setEnabled(false);
        
        bestAlgorithmLabel = new JLabel("En İyi Algoritma: -");
        bestAlgorithmLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        bestAlgorithmLabel.setForeground(Color.BLUE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Üst panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(selectDatasetButton);
        topPanel.add(datasetLabel);
        add(topPanel, BorderLayout.NORTH);
        
        // Orta panel - sonuçlar ve girdiler
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Sol taraf - sonuçlar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Algoritma Test Sonuçları"));
        leftPanel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        leftPanel.add(bestAlgorithmLabel, BorderLayout.SOUTH);
        
        // Sağ taraf - girdiler
        JScrollPane inputScrollPane = new JScrollPane(inputPanel);
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        centerPanel.add(leftPanel);
        centerPanel.add(inputScrollPane);
        add(centerPanel, BorderLayout.CENTER);
        
        // Alt panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(discoverButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        selectDatasetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDataset();
            }
        });
        
        discoverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performClassification();
            }
        });
    }
    
    private void selectDataset() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("ARFF files", "arff"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadDataset(selectedFile);
        }
    }
    
    private void loadDataset(File file) {
        try {
            // Dataset yükle
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(file.getAbsolutePath());
            dataset = source.getDataSet();
            
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
            
            datasetLabel.setText("Dataset: " + file.getName() + 
                " (" + dataset.numInstances() + " instances, " + 
                dataset.numAttributes() + " attributes)");
            
            // Algoritmaları test et
            testAllAlgorithms();
            
            // Input panelini oluştur
            createInputPanel();
            
            discoverButton.setEnabled(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Dataset yüklenirken hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void testAllAlgorithms() {
        resultsArea.setText("Algoritmalar test ediliyor...\n\n");
        
        try {
            Map<String, Double> results = classificationEngine.testAllAlgorithms(dataset);
            
            StringBuilder sb = new StringBuilder();
            sb.append("ALGORITMA TEST SONUÇLARI\n");
            sb.append("=".repeat(50) + "\n\n");
            
            bestAccuracy = 0;
            bestAlgorithm = "";
            
            for (Map.Entry<String, Double> entry : results.entrySet()) {
                String algorithm = entry.getKey();
                Double accuracy = entry.getValue();
                
                if (accuracy != null) {
                    sb.append(String.format("%-20s: %.2f%%\n", algorithm, accuracy));
                    
                    if (accuracy > bestAccuracy) {
                        bestAccuracy = accuracy;
                        bestAlgorithm = algorithm;
                    }
                } else {
                    sb.append(String.format("%-20s: UYGULANAMAZ\n", algorithm));
                }
            }
            
            sb.append("\n" + "=".repeat(50) + "\n");
            sb.append(String.format("EN İYİ: %s (%.2f%%)\n", bestAlgorithm, bestAccuracy));
            // Doğru sınıflandırılan instance sayısı
            int correct = (int) Math.round(dataset.numInstances() * bestAccuracy / 100.0);
            sb.append(String.format("Doğru Sınıflandırılan Instance Sayısı: %d / %d\n", correct, dataset.numInstances()));
            
            resultsArea.setText(sb.toString());
            bestAlgorithmLabel.setText("En İyi Algoritma: " + bestAlgorithm + 
                String.format(" (%.2f%%)", bestAccuracy));
            
        } catch (Exception e) {
            resultsArea.setText("Algoritma testi sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createInputPanel() {
        inputPanel.removeAll();
        inputComponents.clear();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Sınıf atributu hariç tüm atributlar için girdi oluştur
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (i == dataset.classIndex()) continue;
            
            weka.core.Attribute attr = dataset.attribute(i);
            
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel label = new JLabel(attr.name() + ":");
            inputPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            
            JComponent component;
            
            if (attr.isNominal()) {
                // Nominal atribut için ComboBox
                JComboBox<String> comboBox = new JComboBox<>();
                for (int j = 0; j < attr.numValues(); j++) {
                    comboBox.addItem(attr.value(j));
                }
                component = comboBox;
            } else if (attr.isNumeric()) {
                // Numerik atribut için TextField
                JTextField textField = new JTextField(10);
                component = textField;
            } else {
                // String atribut için TextField
                JTextField textField = new JTextField(10);
                component = textField;
            }
            
            inputComponents.put(attr.name(), component);
            inputPanel.add(component, gbc);
            
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
        }
        
        inputPanel.revalidate();
        inputPanel.repaint();
    }
    
    private void performClassification() {
        if (bestAlgorithm == null || bestAlgorithm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Önce bir dataset seçin ve algoritmaların test edilmesini bekleyin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Kullanıcı girdilerinden instance oluştur
            double[] values = instanceCreator.createInstance(dataset, inputComponents);
            
            // En iyi algoritma ile tahmin yap
            String prediction = classificationEngine.classifyInstance(
                dataset, bestAlgorithm, values);
            
            JOptionPane.showMessageDialog(this,
                "Tahmin Edilen Sınıf: " + prediction + 
                "\nKullanılan Algoritma: " + bestAlgorithm,
                "Sınıflandırma Sonucu",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Sınıflandırma sırasında hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WekaClassificationGUI().setVisible(true);
            }
        });
    }
}