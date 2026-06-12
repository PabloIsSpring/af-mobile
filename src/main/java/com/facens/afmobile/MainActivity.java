package com.facens.afmobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facens.afmobile.adapter.VisitaAdapter;
import com.facens.afmobile.model.Visita;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtTitulo, edtDescricao, edtData;
    private Spinner spnCategoria;
    private CheckBox cbFavorito;
    private Button btnBuscarClima, btnSalvar;
    private TextView apiLabel;
    private RecyclerView recyclerView;

    private List<Visita> listaVisitas = new ArrayList<>();
    private VisitaAdapter visitaAdapter;
    private Visita visitaEditando;

    private FusedLocationProviderClient locationProviderClient;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    double longitude;
    double latitude;
    String condicaoTempo;
    String temperaturaAtual;
    boolean climaBuscado;
    int codigoClima;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtTitulo = findViewById(R.id.edtTextTitulo);
        edtData = findViewById(R.id.edtTextData);
        edtDescricao = findViewById(R.id.edtTextDescricao);
        spnCategoria = findViewById(R.id.spnCategoria);
        cbFavorito = findViewById(R.id.cbFavorito);
        apiLabel = findViewById(R.id.textClima);
        recyclerView = findViewById(R.id.recyclerVisita);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        visitaAdapter = new VisitaAdapter(listaVisitas);
        recyclerView.setAdapter(visitaAdapter);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarVisita());
        findViewById(R.id.btnClima).setOnClickListener(v -> capturarLocalizacao());
        carregarVisitas();

        ArrayAdapter<CharSequence> adaptery = ArrayAdapter.createFromResource(
                this, R.array.categoria, android.R.layout.simple_spinner_item);
        adaptery.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(adaptery);
    }

    private void limparCampos() {
        edtTitulo.setText("");
        edtDescricao.setText("");
        edtData.setText("");
        spnCategoria.setSelection(0);
        cbFavorito.setChecked(false);

        climaBuscado = false;
        latitude = 0;
        longitude = 0;
        temperaturaAtual = null;
        condicaoTempo = null;
        codigoClima = 0;

        climaBuscado = false;
        visitaEditando = null;

        apiLabel.setText("Clima ainda não buscado");

        ((Button) findViewById(R.id.btnSalvar)).setText("Salvar Visita");
    }

    @SuppressLint("MissingPermission")
    private void capturarLocalizacao() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    200);
            return;
        }

        Toast.makeText(this, "Capturando localização...", Toast.LENGTH_SHORT).show();

        locationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(l -> {
                    if(l != null) {
                        latitude = l.getLatitude();
                        longitude = l.getLongitude();

                        buscarClima(latitude, longitude);
                    } else {
                        Toast.makeText(this, "Não foi possível capturar a localização.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao capturar localização.", Toast.LENGTH_SHORT).show();
                });
    }

    private void buscarClima(double latitude, double longitude){
        apiLabel.setText("Buscando clima");

        executorService.execute(() -> {
            try {
                String endereco = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&current=temperature_2m,weather_code,wind_speed_10m"
                        + "&timezone=auto";

                URL url = new URL(endereco);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseStatusCode = conn.getResponseCode();
                if(responseStatusCode == 200) {
                    BufferedReader inp = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inpLine;
                    StringBuilder response = new StringBuilder();

                    while((inpLine = inp.readLine()) != null) {
                        response.append(inpLine);
                    }
                    inp.close();

                    String result = response.toString();

                    JSONObject json = new JSONObject(result);
                    JSONObject atual = json.getJSONObject("current");

                    double temperatura = atual.getDouble("temperature_2m");
                    double vento = atual.getDouble("wind_speed_10m");
                    codigoClima = atual.getInt("weather_code");
                    condicaoTempo = condicaoClima(codigoClima);

                    handler.post(() -> {
                        temperaturaAtual = String.format(Locale.getDefault(), "%.1f", temperatura);
                        climaBuscado = true;

                        apiLabel.setText("Clima e GPS carregados com sucesso!");

                        Toast.makeText(this, "GPS e clima carregados!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    apiLabel.setText("Erro ao buscar clima.");
                    Toast.makeText(this, "Erro na API de clima.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void salvarVisita() {
        String titulo = edtTitulo.getText().toString();
        String descricao = edtDescricao.getText().toString();
        String data = edtData.getText().toString();
        String categoria = spnCategoria.getSelectedItem().toString();
        boolean favorito = cbFavorito.isChecked();

        if(visitaEditando == null) {
            Visita v = new Visita(
              titulo,
              descricao,
              data,
              categoria,
              favorito,
              latitude,
              longitude,
              temperaturaAtual,
              codigoClima,
              condicaoTempo
            );

            if(!validarCampos(v)) return;

            db.collection("visitas")
                    .add(v)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Visita salva!", Toast.LENGTH_SHORT).show();
                        climaBuscado = false;
                        limparCampos();
                        carregarVisitas();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao salvar visita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            visitaEditando.setTitulo(titulo);
            visitaEditando.setDescricao(descricao);
            visitaEditando.setData(data);
            visitaEditando.setCategoria(categoria);
            visitaEditando.setFavorito(favorito);

            validarCampos(visitaEditando);

            db.collection("visitas")
                    .document(visitaEditando.getId())
                    .set(visitaEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Visita atualizada!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarVisitas();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao atualizar visita: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void deletarVisita(Visita v, int position) {

        if (v.getId() == null || v.getId().isEmpty()) {
            Toast.makeText(this, "ID da visita inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("visitas")
                .document(v.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaVisitas.remove(position);
                    visitaAdapter.notifyItemRemoved(position);

                    if (visitaEditando != null
                            && visitaEditando.getId().equals(v.getId())) {
                        limparCampos();
                    }

                    Toast.makeText(this, "Visita deletada!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Erro ao deletar: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarVisitas() {
        db.collection("visitas")
                .get()
                .addOnSuccessListener(query -> {
                    listaVisitas.clear();
                    for(QueryDocumentSnapshot doc: query){
                        Visita vi = doc.toObject(Visita.class);
                        vi.setId(doc.getId());
                        listaVisitas.add(vi);
                    }
                    visitaAdapter.notifyDataSetChanged();
                });

        visitaAdapter.setOnItemClickListener(vi -> {
            edtTitulo.setText(vi.getTitulo());
            edtDescricao.setText(vi.getDescricao());
            edtData.setText(vi.getData());

            spnCategoria.setSelection(buscarItemPorPosicao(spnCategoria, vi.getCategoria()));

            cbFavorito.setChecked(vi.isFavorito());
            visitaEditando = vi;
            ((Button) findViewById(R.id.btnSalvar)).setText("Atualizar Visita");
        });

        visitaAdapter.setOnItemDeleteListener((vi, position) -> {
            deletarVisita(vi, position);
        });
    }

    private String condicaoClima(int codigoClima) {
        if (codigoClima == 0) {
            return "Céu limpo";
        } else if (codigoClima >= 1 && codigoClima <= 3) {
            return "Parcialmente nublado ou nublado";
        } else if (codigoClima == 45 || codigoClima == 48) {
            return "Neblina";
        } else if (codigoClima >= 51 && codigoClima <= 57) {
            return "Garoa";
        } else if (codigoClima >= 61 && codigoClima <= 67) {
            return "Chuva";
        } else if (codigoClima >= 71 && codigoClima <= 77) {
            return "Neve";
        } else if (codigoClima >= 80 && codigoClima <= 82) {
            return "Pancadas de chuva";
        } else if (codigoClima >= 95 && codigoClima <= 99) {
            return "Tempestade";
        } else {
            return "Condição desconhecida";
        }
    }

    private int buscarItemPorPosicao(Spinner spin, String val) {
        for(int i = 0; i < spin.getCount(); i++) {
            String item = spin.getItemAtPosition(i).toString();

            if(item.equalsIgnoreCase(val)) {
                return i;
            }
        }
        return 0;
    }

    private boolean validarCampos(Visita v) {
        String titulo = edtTitulo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String data = edtData.getText().toString().trim();


        if (titulo.isEmpty()) {
            edtTitulo.setError("Informe o título.");
            edtTitulo.requestFocus();
            return false;
        }

        if (descricao.isEmpty()) {
            edtDescricao.setError("Informe a descrição.");
            edtDescricao.requestFocus();
            return false;
        }

        if (data.isEmpty()) {
            edtData.setError("Informe a data.");
            edtData.requestFocus();
            return false;
        }

        if (spnCategoria.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!climaBuscado) {
            Toast.makeText(this, "Gps ainda não foi buscado", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }



}
