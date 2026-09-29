package com.solarmind.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Small RFC-style parser for the flat source files used by the MVP. */
final class SimpleCsv {
  private SimpleCsv() {}
  static List<Row> read(Path path) throws IOException {
    List<String> lines=Files.readAllLines(path,StandardCharsets.UTF_8); if(lines.isEmpty())return List.of(); List<String> headers=parseLine(lines.get(0)); List<Row> rows=new ArrayList<>();
    for(int i=1;i<lines.size();i++){if(lines.get(i).isBlank())continue; List<String> values=parseLine(lines.get(i)); Map<String,String> map=new LinkedHashMap<>(); for(int c=0;c<headers.size()&&c<values.size();c++){String h=headers.get(c).trim(); if(!h.isEmpty())map.put(h,values.get(c).trim());} rows.add(new Row(i+1,map));} return rows;
  }
  static Set<String> headers(Path path)throws IOException{List<String> h=parseLine(Files.readAllLines(path,StandardCharsets.UTF_8).get(0)); Set<String> out=new LinkedHashSet<>(); for(String v:h)if(!v.trim().isEmpty())out.add(v.trim()); return out;}
  private static List<String> parseLine(String line){List<String> out=new ArrayList<>(); StringBuilder current=new StringBuilder(); boolean quoted=false; for(int i=0;i<line.length();i++){char ch=line.charAt(i); if(ch=='"'){if(quoted&&i+1<line.length()&&line.charAt(i+1)=='"'){current.append('"');i++;}else quoted=!quoted;}else if(ch==','&&!quoted){out.add(current.toString());current.setLength(0);}else current.append(ch);}out.add(current.toString());return out;}
  record Row(int line,Map<String,String> values){String get(String header){return values.getOrDefault(header,"");}}
}
