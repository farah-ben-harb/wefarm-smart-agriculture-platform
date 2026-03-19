package services;

import entities.Achat;
import java.util.List;

public interface AchatService {
    List<Achat> getAllAchats();
    Achat getAchatById(int id);
    void addAchat(Achat a);
    void updateAchat(Achat a);
    void deleteAchat(int id) throws Exception;
}