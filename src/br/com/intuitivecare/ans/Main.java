package br.com.intuitivecare.ans;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	// URL base da API publica da ANS para demonstracoes contabeis
	private static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";
	
	public static void main(String[] args) throws Exception {
		
		// Ler o HTML da página principal e busca os anos disponíveis
		String html = readUrl(BASE_URL);
		
		// Regex para encontrar anos no formato 20XX
		Pattern yearPattern = Pattern.compile("(20\\d{2})");
		Matcher yearMatcher = yearPattern.matcher(html);
		
		List<Integer> years = new ArrayList<>();
		
		// Adiciona cada ano encontrado na lista
		while (yearMatcher.find()) {
			years.add(Integer.parseInt(yearMatcher.group(1)));
		}
		
		// Seleciona o ano mais recente
		int latestYear = Collections.max(years);
		System.out.println("Ano mais recente: " + latestYear);
		
		// Monta a URL do ano mais recente
		String yearUrl = BASE_URL + latestYear + "/";
		String yearHtml = readUrl(yearUrl);
		
		// Regex para encontrar arquivos zip do ano mais recente
		Pattern zipPattern = Pattern.compile("\\dT" + latestYear + "\\.zip");
		Matcher zipMatcher = zipPattern.matcher(yearHtml);
		
		List<String> zips = new ArrayList<>();
		
		// Adiciona cada arquivo zip encontrado na lista
		while (zipMatcher.find()) {
			zips.add(zipMatcher.group());
		}
		
		zips = new ArrayList<>(new java.util.HashSet<>(zips));
		
		// Ordena alfabeticamente os arquivos zip
		Collections.sort(zips);
		// Seleciona os 3 últimos trimestres
		List<String> lastThree = zips.subList(Math.max(0, zips.size() - 3), zips.size());
		
		System.out.println("Trimestres encontrados: " + lastThree);
		
		// Cria pasta para downloads
		Files.createDirectories(Paths.get("downloads"));
		
		for (String zip : lastThree) {
			String fileUrl = yearUrl + zip;
			Path dest = Paths.get("downloads" , zip);
			
			// Faz o download do arquivo zip
			try (InputStream in = new URL(fileUrl).openStream()){
				Files.copy(in,  dest, StandardCopyOption.REPLACE_EXISTING);
			}
			
			System.out.println("Baixado: " + zip);
		}
	}
	
	// Metodo auxiliar para ler o conteúdo de uma URL como String
	private static String readUrl(String url) throws Exception{
		try (InputStream in = new URL(url).openStream()){
			return new String(in.readAllBytes());
		}
	}
}
