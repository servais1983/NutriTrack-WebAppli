package com.example.nutritrack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutritrack.adapter.FoodAdapter;
import com.example.nutritrack.database.AppDatabase;
import com.example.nutritrack.database.entity.FoodEntryEntity;
import com.example.nutritrack.dialog.AddFoodDialog;
import com.example.nutritrack.nutrition.FoodItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodFragment extends Fragment implements FoodAdapter.OnFoodItemClickListener, AddFoodDialog.AddFoodDialogListener {

    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView rvFoodList;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems;
    private List<FoodItem> filteredFoodItems;

    private int userId;
    private ExecutorService executor;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupérer l'ID de l'utilisateur depuis les arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getInt("userId", -1);
        } else {
            userId = UserSession.getInstance(getContext()).getUserId();
        }

        // Initialiser l'exécuteur pour les opérations de base de données en arrière-plan
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // Initialiser les listes d'aliments
        foodItems = initializeFoodDatabase();
        filteredFoodItems = new ArrayList<>(foodItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        // Initialiser les vues
        etSearch = view.findViewById(R.id.et_search_food);
        btnSearch = view.findViewById(R.id.btn_search);
        rvFoodList = view.findViewById(R.id.rv_food_list);

        // Configurer le RecyclerView
        foodAdapter = new FoodAdapter(filteredFoodItems, this);
        rvFoodList.setAdapter(foodAdapter);
        rvFoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configurer la recherche
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Non utilisé
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrer la liste pendant la frappe
                filterFoodItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Non utilisé
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = etSearch.getText().toString().trim();
                filterFoodItems(query);
            }
        });

        // Ajouter un bouton pour ajouter un nouvel aliment personnalisé
        Button btnAddCustom = view.findViewById(R.id.btn_add_custom);
        btnAddCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog();
            }
        });

        return view;
    }

    private List<FoodItem> initializeFoodDatabase() {
        // Cette méthode devrait normalement charger les aliments depuis une API ou une base de données
        // Ici, nous créons une liste d'exemples d'aliments
        List<FoodItem> items = new ArrayList<>();

        // Ajouter quelques aliments communs
        items.add(new FoodItem("Poulet grillé (100g)", 165, 31, 0, 3.6f));
        items.add(new FoodItem("Riz blanc cuit (100g)", 130, 2.7f, 28, 0.3f));
        items.add(new FoodItem("Brocoli cuit (100g)", 55, 3.7f, 11, 0.3f));
        items.add(new FoodItem("Saumon (100g)", 208, 20, 0, 13));
        items.add(new FoodItem("Pain complet (tranche)", 81, 4, 15, 1.1f));
        items.add(new FoodItem("Œuf entier (grand)", 72, 6.3f, 0.4f, 5));
        items.add(new FoodItem("Lait demi-écrémé (250ml)", 115, 8, 11, 4));
        items.add(new FoodItem("Avocat (100g)", 160, 2, 9, 14.7f));
        items.add(new FoodItem("Pomme (moyenne)", 95, 0.5f, 25, 0.3f));
        items.add(new FoodItem("Yaourt nature (150g)", 86, 5, 5, 3.5f));
        items.add(new FoodItem("Pâtes (100g cuites)", 131, 5, 25, 1.1f));
        items.add(new FoodItem("Thon en conserve (100g)", 116, 25, 0, 1));
        items.add(new FoodItem("Fromage cheddar (30g)", 113, 7, 0.4f, 9.3f));
        items.add(new FoodItem("Banane (moyenne)", 105, 1.3f, 27, 0.4f));
        items.add(new FoodItem("Amandes (30g)", 173, 6, 6, 15));
        items.add(new FoodItem("Chocolat noir (30g)", 170, 2, 13, 12));
        items.add(new FoodItem("Quinoa cuit (100g)", 120, 4.4f, 21, 1.9f));
        items.add(new FoodItem("Lentilles cuites (100g)", 116, 9, 20, 0.4f));
        items.add(new FoodItem("Huile d'olive (1 c.à.s)", 119, 0, 0, 14));
        items.add(new FoodItem("Patate douce (100g)", 86, 1.6f, 20, 0.1f));

        return items;
    }

    private void filterFoodItems(String query) {
        filteredFoodItems.clear();
        
        if (query.isEmpty()) {
            // Si la requête est vide, afficher tous les aliments
            filteredFoodItems.addAll(foodItems);
        } else {
            // Filtrer les aliments par nom
            String lowerCaseQuery = query.toLowerCase();
            for (FoodItem food : foodItems) {
                if (food.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredFoodItems.add(food);
                }
            }
        }
        
        // Mettre à jour le RecyclerView
        foodAdapter.notifyDataSetChanged();
        
        // Afficher un message si aucun aliment n'est trouvé
        if (filteredFoodItems.isEmpty()) {
            Toast.makeText(getContext(), "Aucun aliment trouvé. Essayez une autre recherche ou ajoutez un aliment personnalisé.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddFoodDialog() {
        AddFoodDialog dialog = new AddFoodDialog();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "AddFoodDialog");
    }

    @Override
    public void onFoodItemClick(FoodItem foodItem) {
        // Lorsqu'un aliment est sélectionné, ajouter une entrée dans le journal alimentaire
        saveFoodEntry(foodItem, 100.0f); // 100g par défaut
    }

    @Override
    public void onAddFood(String name, int calories, float protein, float carbs, float fat) {
        // Créer un nouvel aliment personnalisé
        FoodItem newFood = new FoodItem(name, calories, protein, carbs, fat);
        
        // Ajouter à la liste d'aliments
        foodItems.add(newFood);
        filteredFoodItems.add(newFood);
        foodAdapter.notifyDataSetChanged();
        
        // Ajouter automatiquement au journal alimentaire
        saveFoodEntry(newFood, 100.0f); // 100g par défaut
    }

    private void saveFoodEntry(FoodItem food, float quantity) {
        // Ajuster les valeurs nutritionnelles en fonction de la quantité
        float quantityRatio = quantity / 100.0f; // Base de calcul pour 100g
        
        final FoodEntryEntity entry = new FoodEntryEntity();
        entry.setUserId(userId);
        entry.setName(food.getName());
        entry.setCalories(Math.round(food.getCalories() * quantityRatio));
        entry.setProtein(food.getProtein() * quantityRatio);
        entry.setCarbs(food.getCarbs() * quantityRatio);
        entry.setFat(food.getFat() * quantityRatio);
        entry.setQuantity(quantity);
        entry.setDate(new Date()); // Date actuelle
        entry.setMealType("lunch"); // Par défaut, à modifier selon le contexte
        
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Sauvegarder dans la base de données
                long result = AppDatabase.getInstance(getContext()).foodEntryDao().insert(entry);
                
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (result > 0) {
                            Toast.makeText(getContext(), R.string.success_food_added, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Erreur lors de l'ajout de l'aliment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}