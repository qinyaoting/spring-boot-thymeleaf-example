package com.bezkoder.spring.thymeleaf.controller;

import java.util.ArrayList;
import java.util.List;

import com.bezkoder.spring.thymeleaf.tool.SerialPortManager;
import com.fazecast.jSerialComm.SerialPort;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.BaseEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;
import com.bezkoder.spring.thymeleaf.repository.TutorialRepository;

@Controller
public class TutorialController {

  @Autowired
  private TutorialRepository tutorialRepository;

  @GetMapping("/tutorials")
  public String getAll(Model model, @Param("keyword") String keyword) {
    try {
      List<Tutorial> tutorials = new ArrayList<Tutorial>();

      if (keyword == null) {
        tutorialRepository.findAll().forEach(tutorials::add);
      } else {
        tutorialRepository.findByTitleContainingIgnoreCase(keyword).forEach(tutorials::add);
        model.addAttribute("keyword", keyword);
      }

      model.addAttribute("tutorials", tutorials);
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
    }

    return "tutorials";
  }

  @GetMapping("/tutorials/new")
  public String addTutorial(Model model) {
    Tutorial tutorial = new Tutorial();
    tutorial.setPublished(true);

    model.addAttribute("tutorial", tutorial);
    model.addAttribute("pageTitle", "Create new Tutorial");

    return "tutorial_form";
  }

  @PostMapping("/tutorials/save")
  public String saveTutorial(Tutorial tutorial, RedirectAttributes redirectAttributes) {
    try {

      String cmd = getCmdFromTextDisplayTwoLines(tutorial.getTitle());
      SerialPort serialPort = null;
        //cmd = "A5 68 32 01 7B 01 13 00 00 00 02 00 00 00 00 00 03 12 00 34 12 00 35 12 00 36 00 00 00 04 02  AE".replace(" ","");
        try {
          serialPort = SerialPortManager.openPort("/dev/ttyUSB0", 115200);
          byte[] by6 = BaseEncoding.base16().decode(cmd);
          SerialPortManager.sendToPort(serialPort, by6);
          Thread.sleep(1000);
          byte[] bytes1 = SerialPortManager.readFromPort(serialPort);
          String rs = com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex.encodeHexString(bytes1);
        } catch (Exception e) {

        } finally {
          SerialPortManager.closePort(serialPort);
        }


     







      tutorialRepository.save(tutorial);

      redirectAttributes.addFlashAttribute("message", "The Tutorial has been saved successfully!");
    } catch (Exception e) {
      redirectAttributes.addAttribute("message", e.getMessage());
    }

    return "redirect:/tutorials";
  }



  @GetMapping("/tutorials/{id}")
  public String editTutorial(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Tutorial tutorial = tutorialRepository.findById(id).get();

      model.addAttribute("tutorial", tutorial);
      model.addAttribute("pageTitle", "Edit Tutorial (ID: " + id + ")");

      return "tutorial_form";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());

      return "redirect:/tutorials";
    }
  }

  @GetMapping("/tutorials/delete/{id}")
  public String deleteTutorial(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      tutorialRepository.deleteById(id);

      redirectAttributes.addFlashAttribute("message", "The Tutorial with id=" + id + " has been deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/tutorials";
  }

  @GetMapping("/tutorials/{id}/published/{status}")
  public String updateTutorialPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean published,
      Model model, RedirectAttributes redirectAttributes) {
    try {
      tutorialRepository.updatePublishedStatus(id, published);

      String status = published ? "published" : "disabled";
      String message = "The Tutorial id=" + id + " has been " + status;

      redirectAttributes.addFlashAttribute("message", message);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/tutorials";
  }

  public static String getCmdFromTextDisplayTwoLines(String text) {
    try {
      String textMsg = appendFontSizeColor(text, 2);
      String cmdPart2 = String.format("02 00 00 00 00 00 03 %s 00 00 00", textMsg);
      int textLen = hexString2Bytes(cmdPart2).length;
      String packageLen = Integer.toHexString(textLen);
      String cmdPart = "68 32 01 7B 01 %s 00 00 00 %s";  //第一个是包长度, 第二个是包内容
      String cmds = String.format(cmdPart, packageLen, cmdPart2);
      //String str = Hex.encodeHexString(cmds.getBytes());
      byte[] b = hexString2Bytes(cmds);         //16进制转二进制数组
      short c = sumCheck(b); //校验和
      byte[] checkSumArr = shortToByteLittle(c); // 小端模式
      String checkSum = bytes2HexString(checkSumArr);
      String cmd = "A5" + cmds + checkSum + "AE";
      return cmd.replace(" ", "");
    } catch ( Exception e) {
      //Log.e("getCmdFromTextDisplayTwolines","="+e.getMessage());
    }
    return "";
  }

  public static byte[] hexString2Bytes(String hex) {
    hex = hex.replace(" ","");
    if ((hex == null) || (hex.equals(""))){
      return null;
    }
    else if (hex.length()%2 != 0){
      return null;
    }
    else{
      hex = hex.toUpperCase();
      int len = hex.length()/2;
      byte[] b = new byte[len];
      char[] hc = hex.toCharArray();
      for (int i=0; i<len; i++){
        int p=2*i;
        b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p+1]));
      }
      return b;
    }

  }
  private static byte charToByte(char c) {
    return (byte) "0123456789ABCDEF".indexOf(c);
  }
  public static String appendFontSizeColor(String word, int fontSize) {
    StringBuilder sb = new StringBuilder();
    for (char c : word.toCharArray()) {
      String sch = String.valueOf(c);
      String hex = com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex.encodeHexString(sch.getBytes());
      sb.append("1").append(fontSize).append(" 00 ").append(hex).append(" ");
    }
    return sb.toString().toUpperCase();
  }

  public static short sumCheck(byte[] b) {

    int sum = 0;
    for (byte value : b) {
      //&上0xff 转换成无符号整型
      sum = sum + (value & 0xff);
    }
    return (short) (sum & 0xffff);
  }
  public static byte[] shortToByteLittle(short s) {

    byte[] b = new byte[2];
    b[0] = (byte) (s & 0xff);
    b[1] = (byte) (s >> 8 & 0xff);
    return b;
  }
  public static String bytes2HexString(byte[] b) {
    String r = "";

    for (int i = 0; i < b.length; i++) {
      String hex = Integer.toHexString(b[i] & 0xFF);
      if (hex.length() == 1) {
        hex = '0' + hex;
      }
      r += hex.toUpperCase();
    }
    return r;
  }
}
