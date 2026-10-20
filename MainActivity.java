package com.chaibikaner.pos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity {
    private final String PREF = "chai_bikaner_data";
    private LinearLayout root, content, nav;
    private String page = "Billing";
    private JSONArray products, sales, expenses;
    private final HashMap<String,Integer> cart = new HashMap<>();
    private final HashMap<String,Double> cartPrices = new HashMap<>();
    private final int brown = 0xFF5B2C1E, saffron = 0xFFE59A35, cream = 0xFFFFF8EE, ink = 0xFF2C211C;

    @Override public void onCreate(Bundle b) { super.onCreate(b); loadData(); buildShell(); showPage(); }
    private void loadData() {
        String p=getPreferences(0).getString("products", null), s=getPreferences(0).getString("sales", "[]"), e=getPreferences(0).getString("expenses", "[]");
        try { products = p==null ? defaultProducts() : new JSONArray(p); sales=new JSONArray(s); expenses=new JSONArray(e); }
        catch(Exception ex){ products=defaultProducts(); sales=new JSONArray(); expenses=new JSONArray(); }
        saveData();
    }
    private JSONArray defaultProducts(){ JSONArray a=new JSONArray(); addProduct(a,"Bikaneri Chai",10,200); addProduct(a,"Gud Chai",20,100); addProduct(a,"Special Chai",20,100); addProduct(a,"Kulhad Chai",15,100); addProduct(a,"Masala Chai",20,100); addProduct(a,"Lemon Tea",10,100); addProduct(a,"Special Coffee",20,60); addProduct(a,"Black Coffee",15,60); addProduct(a,"Cold Coffee",59,30); addProduct(a,"Bun Maska",25,40); addProduct(a,"Veg Sandwich",40,30); addProduct(a,"Samosa",15,50); return a; }
    private void addProduct(JSONArray a,String n,double price,int stock){ try{ JSONObject o=new JSONObject(); o.put("name",n);o.put("price",price);o.put("stock",stock);a.put(o);}catch(Exception ignored){} }
    private void saveData(){ getPreferences(0).edit().putString("products",products.toString()).putString("sales",sales.toString()).putString("expenses",expenses.toString()).apply(); }
    private void buildShell(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(cream); setContentView(root);
        LinearLayout header=new LinearLayout(this); header.setPadding(18,18,18,14); header.setGravity(Gravity.CENTER_VERTICAL); header.setBackgroundColor(brown); header.setOrientation(LinearLayout.VERTICAL);
        TextView title=text("CHAI BIKANER",24,true,0xFFFFFFFF); header.addView(title); TextView sub=text("CAFE BILLING & POS  •  OFFLINE",12,false,0xFFFFD8A5); header.addView(sub); root.addView(header);
        nav=new LinearLayout(this); nav.setPadding(6,8,6,8); nav.setBackgroundColor(0xFFFFEBD0); String[] pages={"Billing","Stock","Sales","Expenses"};
        for(String x:pages){ Button b=button(x); b.setTextSize(12); b.setPadding(5,9,5,9); nav.addView(b,new LinearLayout.LayoutParams(0,-2,1)); b.setOnClickListener(v->{page=x;showPage();}); } root.addView(nav);
        ScrollView scroll=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(14,12,14,22); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }
    private TextView text(String s,int size,boolean bold,int color){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(2,5,2,5);return t; }
    private Button button(String s){ Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(0xFFFFFFFF);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(brown));return b; }
    private Button softButton(String s){ Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(brown);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFE0B2));return b; }
    private void gap(){ View v=new View(this);content.addView(v,new LinearLayout.LayoutParams(1,7)); }
    private void line(){ View v=new View(this);v.setBackgroundColor(0xFFE7D7C6);content.addView(v,new LinearLayout.LayoutParams(-1,1)); }
    private String money(double n){return "₹"+String.format(Locale.getDefault(),"%.2f",n).replaceAll("\\.00$","");}
    private JSONObject productAt(int i){return products.optJSONObject(i);}
    private int findProduct(String name){for(int i=0;i<products.length();i++)if(name.equals(products.optJSONObject(i).optString("name")))return i;return -1;}
    private void showPage(){content.removeAllViews(); if(page.equals("Billing"))billingPage();else if(page.equals("Stock"))stockPage();else if(page.equals("Sales"))salesPage();else expensesPage();}
    private double total(){double t=0;for(Map.Entry<String,Integer> e:cart.entrySet())t+=cartPrices.get(e.getKey())*e.getValue();return t;}
    private void billingPage(){
        content.addView(text("New bill",22,true,ink)); content.addView(text("Tap an item to add it to the order",13,false,0xFF75665D));
        for(int i=0;i<products.length();i++){JSONObject p=productAt(i); if(p==null)continue; String name=p.optString("name");double price=p.optDouble("price");int stock=p.optInt("stock");
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(10,7,6,7);row.setBackgroundColor(0xFFFFFFFF);
            LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.addView(text(name,15,true,ink));labels.addView(text(money(price)+"  •  Stock: "+stock,12,false,0xFF78675D));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1)); Button add=softButton("+ Add");add.setEnabled(stock>0);row.addView(add);add.setOnClickListener(v->{cart.put(name,cart.getOrDefault(name,0)+1);cartPrices.put(name,price);showPage();});content.addView(row);gap(); }
        line();content.addView(text("CURRENT ORDER",17,true,brown));
        if(cart.isEmpty())content.addView(text("No items added yet.",14,false,0xFF75665D));
        for(String name:new ArrayList<>(cart.keySet())){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.addView(text(name+"  × "+cart.get(name),14,true,ink),new LinearLayout.LayoutParams(0,-2,1));row.addView(text(money(cartPrices.get(name)*cart.get(name)),14,true,brown));Button minus=softButton("−");row.addView(minus);minus.setOnClickListener(v->{int q=cart.get(name)-1;if(q<=0){cart.remove(name);cartPrices.remove(name);}else cart.put(name,q);showPage();});content.addView(row);}
        content.addView(text("TOTAL  "+money(total()),23,true,brown));Button checkout=button("Complete Sale & Save Bill");checkout.setEnabled(!cart.isEmpty());content.addView(checkout);checkout.setOnClickListener(v->checkout());
        Button print=softButton("Print Last Receipt (Bluetooth)");content.addView(print);print.setOnClickListener(v->printLastReceipt());
    }
    private void checkout(){
        for(String n:cart.keySet()){int i=findProduct(n);if(i<0||productAt(i).optInt("stock")<cart.get(n)){toast("Not enough stock for "+n);return;}}
        try{JSONObject sale=new JSONObject();sale.put("id","CB-"+new SimpleDateFormat("yyMMdd-HHmmss",Locale.US).format(new Date()));sale.put("time",new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date()));sale.put("epoch",System.currentTimeMillis());sale.put("total",total());JSONArray lines=new JSONArray();for(String n:cart.keySet()){int i=findProduct(n);JSONObject p=productAt(i);p.put("stock",p.optInt("stock")-cart.get(n));JSONObject line=new JSONObject();line.put("name",n);line.put("qty",cart.get(n));line.put("price",cartPrices.get(n));lines.put(line);}sale.put("items",lines);sales.put(sale);saveData();cart.clear();cartPrices.clear();toast("Sale saved successfully");showPage();}catch(Exception ex){toast("Could not save sale");}
    }
    private void stockPage(){content.addView(text("Inventory & menu",22,true,ink));content.addView(text("Update stock or add a new menu item. Data stays on this device.",13,false,0xFF75665D));Button add=button("+ Add Menu Item");content.addView(add);add.setOnClickListener(v->addProductDialog());gap();
        for(int i=0;i<products.length();i++){JSONObject p=productAt(i);if(p==null)continue;final String name=p.optString("name");LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(10,8,10,8);card.setBackgroundColor(0xFFFFFFFF);card.addView(text(name,16,true,ink));card.addView(text("Price: "+money(p.optDouble("price"))+"     In stock: "+p.optInt("stock"),13,false,0xFF6B5C52));LinearLayout actions=new LinearLayout(this);Button restock=softButton("Restock");Button edit=softButton("Edit price");actions.addView(restock,new LinearLayout.LayoutParams(0,-2,1));actions.addView(edit,new LinearLayout.LayoutParams(0,-2,1));card.addView(actions);content.addView(card);gap();restock.setOnClickListener(v->numberDialog("Restock "+name,"Quantity to add",q->{int idx=findProduct(name);try{products.getJSONObject(idx).put("stock",products.getJSONObject(idx).optInt("stock")+Integer.parseInt(q));saveData();showPage();}catch(Exception ex){toast("Enter a whole number");}}));edit.setOnClickListener(v->numberDialog("Price for "+name,"New price in rupees",q->{int idx=findProduct(name);try{double val=Double.parseDouble(q);if(val<0)throw new Exception();products.getJSONObject(idx).put("price",val);saveData();showPage();}catch(Exception ex){toast("Enter a valid price");}}));}
    }
    interface TextCallback{void done(String value);}
    private void numberDialog(String title,String hint,TextCallback callback){EditText input=new EditText(this);input.setSingleLine(true);input.setHint(hint);new AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->callback.done(input.getText().toString().trim())).show();}
    private void addProductDialog(){LinearLayout box=new LinearLayout(this);box.setPadding(20,8,20,0);box.setOrientation(LinearLayout.VERTICAL);EditText name=new EditText(this);name.setHint("Item name");EditText price=new EditText(this);price.setHint("Price in rupees");price.setInputType(8194);EditText stock=new EditText(this);stock.setHint("Opening stock quantity");stock.setInputType(2);box.addView(name);box.addView(price);box.addView(stock);new AlertDialog.Builder(this).setTitle("New menu item").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Add",(d,w)->{try{String n=name.getText().toString().trim();double p=Double.parseDouble(price.getText().toString());int s=Integer.parseInt(stock.getText().toString());if(n.isEmpty()||p<0||s<0)throw new Exception();if(findProduct(n)>=0){toast("That item already exists");return;}addProduct(products,n,p,s);saveData();showPage();}catch(Exception ex){toast("Please enter a name, price and stock");}}).show();}
    private void salesPage(){content.addView(text("Sales report",22,true,ink));double today=0,all=0;long start=System.currentTimeMillis()-24*60*60*1000L;for(int i=0;i<sales.length();i++){JSONObject s=sales.optJSONObject(i);if(s!=null){all+=s.optDouble("total");if(s.optLong("epoch")>=start)today+=s.optDouble("total");}}content.addView(text("Last 24 hours:  "+money(today),18,true,brown));content.addView(text("All-time sales:  "+money(all),16,true,ink));content.addView(text("Completed bills: "+sales.length(),14,false,ink));gap();content.addView(text("Recent bills",18,true,ink));for(int i=sales.length()-1;i>=0;i--){JSONObject s=sales.optJSONObject(i);if(s==null)continue;content.addView(text(s.optString("id")+"  •  "+money(s.optDouble("total")),15,true,ink));content.addView(text(s.optString("time"),12,false,0xFF75665D));JSONArray items=s.optJSONArray("items");StringBuilder b=new StringBuilder();if(items!=null)for(int j=0;j<items.length();j++){JSONObject it=items.optJSONObject(j);if(it!=null)b.append(it.optString("name")).append(" × ").append(it.optInt("qty")).append("   ");}content.addView(text(b.toString(),12,false,0xFF75665D));line();}}
    private void expensesPage(){content.addView(text("Expenses",22,true,ink));double sum=0;for(int i=0;i<expenses.length();i++)sum+=expenses.optJSONObject(i).optDouble("amount");content.addView(text("Recorded expenses: "+money(sum),18,true,brown));Button add=button("+ Add Expense");content.addView(add);add.setOnClickListener(v->expenseDialog());gap();for(int i=expenses.length()-1;i>=0;i--){JSONObject e=expenses.optJSONObject(i);if(e!=null){content.addView(text(e.optString("title")+"  —  "+money(e.optDouble("amount")),15,true,ink));content.addView(text(e.optString("time"),12,false,0xFF75665D));line();}}}
    private void expenseDialog(){LinearLayout box=new LinearLayout(this);box.setPadding(20,8,20,0);box.setOrientation(LinearLayout.VERTICAL);EditText title=new EditText(this);title.setHint("Expense (milk, gas, rent...)");EditText amount=new EditText(this);amount.setHint("Amount in rupees");amount.setInputType(8194);box.addView(title);box.addView(amount);new AlertDialog.Builder(this).setTitle("Record expense").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{try{String t=title.getText().toString().trim();double a=Double.parseDouble(amount.getText().toString());if(t.isEmpty()||a<=0)throw new Exception();JSONObject e=new JSONObject();e.put("title",t);e.put("amount",a);e.put("time",new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date()));expenses.put(e);saveData();showPage();}catch(Exception ex){toast("Enter a description and valid amount");}}).show();}
    private void printLastReceipt(){if(sales.length()==0){toast("Complete a sale first");return;}if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},81);toast("Allow Bluetooth access, then tap Print again");return;}try{BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();if(adapter==null){toast("This device has no Bluetooth");return;}if(!adapter.isEnabled()){toast("Turn on Bluetooth first");return;}ArrayList<BluetoothDevice> devices=new ArrayList<>(adapter.getBondedDevices());if(devices.isEmpty()){toast("Pair your thermal printer in Android Bluetooth settings first");return;}String[] names=new String[devices.size()];for(int i=0;i<devices.size();i++)names[i]=devices.get(i).getName()+"\n"+devices.get(i).getAddress();new AlertDialog.Builder(this).setTitle("Choose paired printer").setItems(names,(d,which)->new Thread(()->sendReceipt(devices.get(which))).start()).setNegativeButton("Cancel",null).show();}catch(SecurityException ex){toast("Bluetooth permission is needed");}}
    private void sendReceipt(BluetoothDevice device){BluetoothSocket socket=null;try{JSONObject s=sales.getJSONObject(sales.length()-1);socket=device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));socket.connect();OutputStream out=socket.getOutputStream();StringBuilder r=new StringBuilder();r.append("\u001B@").append("       CHAI BIKANER\n").append("       Cafe Receipt\n").append("--------------------------------\n").append(s.optString("id")).append("\n").append(s.optString("time")).append("\n--------------------------------\n");JSONArray a=s.optJSONArray("items");if(a!=null)for(int i=0;i<a.length();i++){JSONObject it=a.getJSONObject(i);r.append(it.optString("name")).append(" x").append(it.optInt("qty")).append("  ").append(money(it.optDouble("price")*it.optInt("qty"))).append("\n");}r.append("--------------------------------\nTOTAL: ").append(money(s.optDouble("total"))).append("\n\nThank you! Visit again.\n\n\n").append("\u001D\u0056\u0000");out.write(r.toString().getBytes("UTF-8"));out.flush();socket.close();runOnUiThread(()->toast("Receipt sent to printer"));}catch(Exception ex){try{if(socket!=null)socket.close();}catch(Exception ignored){}runOnUiThread(()->toast("Printing failed. Check printer is paired and supports Bluetooth SPP."));}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
