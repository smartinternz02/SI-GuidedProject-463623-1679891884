package com.nithin.app.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nithin.app.model.User;
import com.nithin.app.model.UserIp;
import org.springframework.stereotype.Controller;

@Controller
public class FlightController {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	
	
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@GetMapping("/")
	public ModelAndView homeGet(ModelAndView modelAndView)
	{
		modelAndView.setViewName("home");
		return modelAndView;
	}
	@GetMapping("/Register")
	public ModelAndView RegGet(ModelAndView modelAndView,User user)
	{
		modelAndView.addObject("user", user);
		modelAndView.setViewName("register");
		return modelAndView;
	}
	@GetMapping("/Login")
	public ModelAndView LogGet(ModelAndView modelAndView,User user)
	{
	    modelAndView.addObject("user", user);
		modelAndView.setViewName("login");
		return modelAndView;
	}
	
	@PostMapping("/Register")
	public ModelAndView registerUser(ModelAndView modelAndView, User user) {
		String email=user.getEmailId();
		String password=user.getPass();
		String name=user.getName();
		System.out.println(email);
		   String sql = "SELECT COUNT(*) FROM users WHERE emailId=?";
		   int count =  jdbcTemplate.queryForObject(sql, Integer.class, email);
		if(count>0) {

			System.out.println("The user already exists");
			modelAndView.addObject("msg", "The email already exists!");
			modelAndView.setViewName("register");
		} 
		else{
			 sql="INSERT INTO users(name,emailId,pass) VALUES(?,?,?)";
		    int result=jdbcTemplate.update(sql,name,email,password);
			if(result>0){
				modelAndView.addObject("msg","Account Registered Successfully");
				modelAndView.setViewName("login");
			}
			else{
				modelAndView.addObject("msg","Some Error Occured.Try After Some Time");
				modelAndView.setViewName("register");
			}
		}
		return modelAndView;	
	}


	@PostMapping("/Login")
	public ModelAndView loginUser(ModelAndView modelAndView, User user){
      String email=user.getEmailId();
	  String password=user.getPass();
	  
	  System.out.println("the defined password is :"+password);

	  String sql="SELECT COUNT(*) FROM users WHERE emailId=?";
	  int count =  jdbcTemplate.queryForObject(sql, Integer.class, email);
	  if(count>0){
		sql="SELECT * FROM users WHERE emailId='"+email+"'";

		jdbcTemplate.query(sql, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				String passwordentered = resultSet.getString("pass");
				
				System.out.println("the password in database is:"+passwordentered);
				
				if(passwordentered.equals(password)){
					modelAndView.addObject("msg", "Login successful!");
					modelAndView.setViewName("loginHome");
				}
				else{
					modelAndView.addObject("msg", "Invalid Username or Password!");
					modelAndView.setViewName("login");
				}

				return null;
			}
		});
		
		
	  }
	  else{
		modelAndView.addObject("msg", "User doesn't exist!");
			modelAndView.setViewName("login");
	  }

		return modelAndView;
	}


	ApiCalls ap=new ApiCalls();

	@GetMapping("/logHome")
     public ModelAndView loghomeGet(ModelAndView modelAndView,UserIp userip)
     {
	modelAndView.addObject("userip", userip);
	modelAndView.setViewName("loginHome");
	return modelAndView;
     }

	@GetMapping("/fprice")
	public ModelAndView FlightPrices(ModelAndView modelAndView, UserIp userip){
		modelAndView.addObject("userip", userip);
	     modelAndView.setViewName("fprice");
	    return modelAndView;
	}


	@PostMapping(value="/fprice")
public ModelAndView dispform(ModelAndView modelAndView,UserIp userip) throws InterruptedException, IOException, ParseException, UnirestException
{
	String src=userip.getOrigin();
	String dest=userip.getDest();
	String dte=userip.getOutDate();
	JSONObject obj=ap.fprices(src, dest, dte);

	
	ArrayList<Double> pricelist=new ArrayList<Double>();
	ArrayList<String> originlist=new ArrayList<String>();
	ArrayList<String> destinationlist=new ArrayList<String>();


	
    String message=(String)obj.get("message");
    System.out.println("After json parsing message:"+message);
    JSONArray array=(JSONArray)obj.get("data");
    for(int i=0;i<array.length();i++) {
    	
    	JSONObject jsbody=(JSONObject)array.get(i);
    	JSONObject  price=(JSONObject)jsbody.get("price");
    	double amount=price.getDouble("amount");
    	pricelist.add(amount);

    	System.out.println("amount for flight:"+amount);
    	JSONArray legs=(JSONArray)jsbody.get("legs");
    	JSONObject le=(JSONObject)legs.get(0);
    	JSONObject or=(JSONObject)le.get("origin");
		String origin=(String)or.get("name");
    	originlist.add(origin);

    	System.out.println("The source address is:"+origin);
    	JSONObject dst=(JSONObject)le.get("destination");
    	String destiny=(String)dst.get("name");
    	destinationlist.add(destiny);
    	System.out.println("The destination address is:"+destiny);
    	System.out.println("-----------------------------------------");
    	
    	}
  modelAndView.addObject("message",message);
  modelAndView.addObject("msg","Flight Details");
  modelAndView.addObject("pricelist",pricelist);
  modelAndView.addObject("originlist",originlist);
  modelAndView.addObject("destinationlist",destinationlist);
  modelAndView.addObject("userip",userip);
  modelAndView.setViewName("fprice");
	
	return modelAndView;
}

@GetMapping("/dprice")
public ModelAndView dpriceGet(ModelAndView modelAndView,UserIp userip)
{
	modelAndView.addObject("userip", userip);
	modelAndView.setViewName("dprice");
	return modelAndView;
}

@PostMapping("/dprice")
public ModelAndView dipriceform(ModelAndView modelAndView,UserIp userip) throws InterruptedException, IOException, ParseException, UnirestException 
{
	
 
  String src=userip.getOrigin();
  String dest=userip.getDest();
  String dte=userip.getOutDate();
  String dte2=userip.getInDate();
	
	
  
	JSONObject obj=ap.dprices(src, dest, dte,dte2);

	
	ArrayList<Double> pricelist=new ArrayList<Double>();
	ArrayList<String> sourcelist=new ArrayList<String>();
	ArrayList<String> destinationlist=new ArrayList<String>();
	ArrayList<String> departurelist=new ArrayList<String>();
	ArrayList<String> arrivallist=new ArrayList<String>();

	
    String message=(String)obj.get("message");
    System.out.println("After json parsing message:"+message);
    JSONArray array=(JSONArray)obj.get("data");
    for(int i=0;i<array.length();i++) {
    	JSONObject jsbody=(JSONObject)array.get(i);
    	JSONObject  price=(JSONObject)jsbody.get("price");
    	//-------------------------------------
    	double amount=price.getDouble("amount");
    	pricelist.add(amount);


    	System.out.println("amount for flight:"+amount);
    	JSONArray legs=(JSONArray)jsbody.get("legs");
    	JSONObject le=(JSONObject)legs.get(0);
    	JSONObject or=(JSONObject)le.get("origin");
    	//---------------------------------------
    	String origin=(String)or.get("name");
    	sourcelist.add(origin);


    	System.out.println("The source address is:"+origin);
    	JSONObject dst=(JSONObject)le.get("destination");
    	//------------------------------------------
    	String destiny=(String)dst.get("name");
    	destinationlist.add(destiny);


    	System.out.println("The destination address is:"+destiny);
    	//------------------------------------------
    	String arrival=(String)le.get("arrival");
    	arrivallist.add(arrival);
    	String departure=(String)le.get("departure");
		departurelist.add(departure);


    	System.out.println("The departure time is:"+departure);
    	System.out.println("The arrival time is:"+arrival);
    	
    	System.out.println("---------------------------------");
    }
    
	modelAndView.addObject("message",message);
  modelAndView.addObject("msg","Flight Details");
  modelAndView.addObject("pricelist",pricelist);
  modelAndView.addObject("sourcelist",sourcelist);
  modelAndView.addObject("destinationlist",destinationlist);
  modelAndView.addObject("departurelist",departurelist);
  modelAndView.addObject("arrivallist",arrivallist);
	modelAndView.setViewName("index");
	return modelAndView;
}


@GetMapping("/cntrys")
public ModelAndView countryGet(ModelAndView modelAndView)
{
	modelAndView.setViewName("country");
	return modelAndView;
}

@PostMapping("/cntrys")
public ModelAndView cntry(ModelAndView modelAndView) throws IOException, InterruptedException, UnirestException
{
	JSONObject obj=ap.cntrys();
    ArrayList<String> countrylist=new ArrayList<String>();
    ArrayList<String> codelist=new ArrayList<String>();

   JSONArray array=(JSONArray)obj.get("data");
   for(int i=0;i<array.length();i++) {
   	JSONObject jsbody=(JSONObject)array.get(i);
   	//---------------------------------------
   	String code=(String)jsbody.get("code");
   	String countryname=(String)jsbody.get("name");
   	System.out.println("The country code is:"+code);
   	System.out.println("The country name is:"+countryname);
   	System.out.println("--------------------------------");
   	
	countrylist.add(countryname);
	codelist.add(code);

   }
	modelAndView.addObject("countrylist", countrylist);
	modelAndView.addObject("codelist", codelist);
	modelAndView.setViewName("country");
	return modelAndView;
}

@GetMapping("/currs")
public ModelAndView currencyGet(ModelAndView modelAndView)
{
	modelAndView.setViewName("currencies");
	return modelAndView;
}


@PostMapping("/currs")
public ModelAndView currss(ModelAndView modelAndView) throws IOException, InterruptedException, UnirestException
{
   JSONObject obj=ap.curs();
   
   ArrayList<String> countrylist=new ArrayList<String>();
   ArrayList<String> symbollist=new ArrayList<String>();
   
   JSONArray array=(JSONArray)obj.get("data");
   for(int i=0;i<array.length();i++) {
   	JSONObject jsbody=(JSONObject)array.get(i);
   	JSONArray ar=(JSONArray)jsbody.get("countryCodes");
   	//--------------------------------------------
   	String countid=(String)ar.getString(0);
   	String symbol=(String)jsbody.get("symbol");
   	countrylist.add(countid);
   	symbollist.add(symbol);
   	System.out.println("The country code is:"+countid);
   	System.out.println("The symbol is:"+symbol);
   	System.out.println("--------------------------------");
   	
   }


	modelAndView.addObject("countrylist", countrylist);
	modelAndView.addObject("symbollist", symbollist);
	
	modelAndView.setViewName("currencies");
	return modelAndView;
}

@GetMapping("/routes")
public ModelAndView routeGet(ModelAndView modelAndView ,UserIp userip )
{
	modelAndView.addObject("userip", userip);
	modelAndView.setViewName("route");
	return modelAndView;
}


@PostMapping(value="/routes")
public ModelAndView route(ModelAndView modelAndView,UserIp userip) throws InterruptedException, IOException, ParseException, UnirestException
{
	String ct,cr,src,dest,dte;
	ct=userip.getCountry();
	cr=userip.getCurrency();
	src=userip.getOrigin();
	dest=userip.getDest();
	dte=userip.getOutDate();
	
	
  
	JSONObject obj=ap.rts(src, dest, dte,cr,ct);
	
	ArrayList<String> originlist=new ArrayList<String>();
	ArrayList<String> destinationlist=new ArrayList<String>();
	ArrayList<String> departurelist=new ArrayList<String>();
	ArrayList<String> fllist=new ArrayList<String>();
	
    String message=(String)obj.get("message");
    System.out.println("After json parsing message:"+message);
    JSONArray array=(JSONArray)obj.get("data");
    for(int i=0;i<15;i++) {
    	
    	JSONObject jsbody=(JSONObject)array.get(i);

    	JSONArray legs=(JSONArray)jsbody.get("legs");
    	JSONObject le=(JSONObject)legs.get(0);
    	//-----------------------------------
    	JSONObject or=(JSONObject)le.get("origin");
    	//-----------------------------------------
    	String origin=(String)or.get("name");
    	originlist.add(origin);



    	System.out.println("The source address is:"+origin);
    	JSONObject dst=(JSONObject)le.get("destination");
    	//-------------------------------------------
    	String destiny=(String)dst.get("name");
    	destinationlist.add(destiny);


    	String departure=(String)le.get("departure");
    	departurelist.add(departure);


    	JSONArray carriers=(JSONArray)le.getJSONArray("carriers");
    	JSONObject flight=(JSONObject)carriers.get(0);
    	//--------------------------------------------
    	String flname=(String)flight.get("name");
    	fllist.add(flname);


    	System.out.println("The destination address is:"+destiny);
    	System.out.println("Te departure time is:"+departure);
    	System.out.println("The flightname is:"+flname);
    	System.out.println("------------------------------------");

    	
    }
	
    modelAndView.addObject("message",message);
    modelAndView.addObject("msg","Flight Details");
    modelAndView.addObject("originlist",originlist);
    modelAndView.addObject("destinationlist",destinationlist);
    modelAndView.addObject("departurelist",departurelist);
    modelAndView.addObject("fllist",fllist);
     
	modelAndView.setViewName("newroutes");
	return modelAndView;
}


@GetMapping("/places")
public ModelAndView places(ModelAndView modelAndView,UserIp userip) throws IOException, InterruptedException
{
	
	modelAndView.addObject("userip",userip);
	modelAndView.setViewName("places");
	return modelAndView;
}
@PostMapping("/places")
public ModelAndView placespost(ModelAndView modelAndView,UserIp userip) throws IOException, InterruptedException, UnirestException
{
	String code;

	code=userip.getCountry();
	JSONObject obj=ap.places(code);


   ArrayList<String> citylist=new ArrayList<String>();
   ArrayList<String> regionlist=new ArrayList<String>();

   
   JSONArray array=(JSONArray)obj.get("data");
   for(int i=0;i<array.length();i++) {
   	JSONObject jsbody=(JSONObject)array.get(i);
   	String city=(String)jsbody.get("city");
   	citylist.add(city);
   	String region=(String)jsbody.get("region");
    regionlist.add(region);
   	System.out.println("Name of the city:"+city);
   	System.out.println("Name of the region:"+region);
   	System.out.println("-----------------------------");
   	
   }
   
   modelAndView.addObject("msg","City Details");
   modelAndView.addObject("citylist",citylist);
   modelAndView.addObject("regionlist",regionlist);
	modelAndView.addObject("userip",userip);
	modelAndView.setViewName("places");
	return modelAndView;
}

	

	
	
	
	
	
	
	
}
