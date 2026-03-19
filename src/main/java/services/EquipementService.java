package services;

import entities.Equipement;
import java.util.List;

public interface EquipementService {
    List<Equipement> getAllEquipements();
    Equipement getEquipementById(int id);
    void addEquipement(Equipement e);
    void updateEquipement(Equipement e);
    void deleteEquipement(int id) throws Exception;
}