# WekaClassifierApp

![WekaClassifierApp](https://github.com/user-attachments/assets/3ac349e5-11bd-455d-a224-8ea78d3831cc)


Bu proje, Java ve WEKA kütüphanesi kullanılarak geliştirilmiş bir makine öğrenimi sınıflandırma uygulamasıdır. Kullanıcı dostu bir arayüz ile farklı veri kümeleri üzerinde çeşitli sınıflandırma algoritmalarını test etmenizi ve yeni girdilerle tahmin yapmanızı sağlar.

## Özellikler

- ARFF formatında veri kümesi yükleme
- Naive Bayes, Lojistik Regresyon, K-En Yakın Komşu, Karar Ağacı (J48), Rastgele Orman, Rastgele Ağaçlar, Çok Katmanlı Algılayıcı ve Destek Vektör Makineleri ile sınıflandırma
- Algoritmaların doğruluk oranlarını karşılaştırma ve en iyi algoritmayı otomatik seçme
- Kullanıcıdan yeni instance girdileri alarak tahmin yapma
- Sonuçları ve en iyi algoritmayı arayüzde gösterme

## Kurulum ve Çalıştırma

1. **Gereksinimler:**
   - Java JDK 8 veya üzeri
   - [WEKA](https://www.cs.waikato.ac.nz/ml/weka/) kütüphanesi (`lib/weka.jar` dosyası ile birlikte gelir)

2. **Projeyi Derleme ve Çalıştırma:**

   Windows için:
   ```
   compile_and_run.bat
   ```

   Alternatif olarak manuel:
   ```
   javac -cp "lib/weka.jar" -d bin src/main/java/com/weka/classification/*.java
   java --add-opens java.base/java.lang=ALL-UNNAMED -cp "bin;lib/weka.jar" com.weka.classification.WekaClassificationGUI
   ```

3. **Kullanım:**
   - Uygulama açıldığında "Dataset Seç" butonuna tıklayarak bir `.arff` dosyası seçin.
   - Algoritmalar otomatik olarak test edilir ve sonuçlar ekranda gösterilir.
   - Sağdaki panelden yeni instance girdilerini girip "Keşfet" butonuna basarak tahmin alabilirsiniz.

## Dosya Yapısı

- `src/main/java/com/weka/classification/`  
  - `WekaClassificationGUI.java`: Ana arayüz ve uygulama akışı  
  - `ClassificationEngine.java`: Sınıflandırma algoritmalarının yönetimi  
  - `DataPreprocessor.java`: Veri ön işleme işlemleri  
  - `InstanceCreator.java`: Kullanıcı girdilerinden yeni instance oluşturma  
- `lib/weka.jar`: WEKA kütüphanesi  
- `data/`: Örnek veri kümeleri (ARFF dosyaları)  
- `compile_and_run.bat`: Derleme ve çalıştırma betiği

## Katkı ve Lisans

Bu proje eğitim amaçlıdır. Katkıda bulunmak için pull request gönderebilirsiniz.

---

**Not:** Uygulama yalnızca ARFF formatındaki veri kümeleriyle çalışır. Veri kümenizin son sütunu
