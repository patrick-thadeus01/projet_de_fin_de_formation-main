package com.formation.pharmacy_manager.services.commandeService;

import com.formation.pharmacy_manager.dto.commandeDto.*;
import com.formation.pharmacy_manager.dto.drugDto.DrugResponseDto;
import com.formation.pharmacy_manager.entities.Command;
import com.formation.pharmacy_manager.entities.User;
import com.formation.pharmacy_manager.repository.CommandRepository;
import com.formation.pharmacy_manager.repository.UserRepository;
import com.formation.pharmacy_manager.security.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class CommandServiceImpl implements CommandService {

    private final CommandRepository commandRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    public CommandeResponseDto createCommande(CommandeRequestDto dto) {
        // ✅ Sécurité : un non-admin ne peut créer une commande que pour lui-même
        String targetUserName = dto.getUserName();
        if (!securityUtils.isCurrentUserAdmin()) {
            String currentUserName = securityUtils.getCurrentUserName();
            if (!currentUserName.equals(targetUserName)) {
                throw new com.formation.pharmacy_manager.exceptions.ForbiddenException(
                        "Vous ne pouvez créer une commande que pour vous-même.");
            }
        }

        User user = userRepository.findDistinctByUserName(targetUserName);
        if (user == null) {
            throw new RuntimeException("Utilisateur introuvable : " + targetUserName);
        }

        Command command = new Command();
        command.setPseudo(dto.getPseudo());
        command.setUser(user);
        command.setCreation_date(new Date());

        Command cmd = commandRepository.save(command);
        return toDto(cmd);
    }

    @Override
    public List<DrugResponseDto> getListDrugToCommand(String pseudo) {
        // ✅ Sécurité : on vérifie que la commande appartient à l'utilisateur connecté
        Command command = commandRepository.findDistinctByPseudo(pseudo);
        if (command == null) {
            throw new RuntimeException("Commande introuvable : " + pseudo);
        }
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());

        return commandRepository.getListDrugToCommand(pseudo).stream().map(
                dg -> new DrugResponseDto(
                        dg.getDrugId(),
                        dg.getDrugName(),
                        dg.getDrugDescription(),
                        dg.getPeremption(),
                        dg.getPrice(),
                        dg.getCategory().getCategoryType(),
                        dg.getCreation_date(),
                        dg.getUpdate_date()
                )).toList();
    }

    @Override
    public List<CommandeResponseDto> getListCommand() {
        // ✅ Sécurité : un non-admin ne voit que ses propres commandes
        if (securityUtils.isCurrentUserAdmin()) {
            return commandRepository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        }
        String currentUserName = securityUtils.getCurrentUserName();
        return commandRepository.findAll().stream()
                .filter(cmd -> cmd.getUser() != null
                        && currentUserName.equals(cmd.getUser().getUserName()))
                .map(this::toDto)
                .toList();
    }

    @Override
    public String deleteById(long id) {
        Command command = commandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        // ✅ Sécurité
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());

        commandRepository.deleteById(id);
        return "Commande supprimée avec succès";
    }

    @Override
    public List<CommandDate> totalCommandPerDate() {
        // Statistiques globales → réservées aux admins
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new com.formation.pharmacy_manager.exceptions.ForbiddenException(
                    "Accès réservé aux administrateurs.");
        }
        return commandRepository.totalCommandPassPerDate();
    }

    @Override
    public List<TotalMoneyPerCommand> totalRevenuCommand() {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new com.formation.pharmacy_manager.exceptions.ForbiddenException(
                    "Accès réservé aux administrateurs.");
        }
        return commandRepository.totalRevenuCommand();
    }

    @Override
    public List<TotalQuantityForDrugCommand> totalQuantityDrugInCommandDrug() {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new com.formation.pharmacy_manager.exceptions.ForbiddenException(
                    "Accès réservé aux administrateurs.");
        }
        return commandRepository.totalQuantityDrugInCommandDrug();
    }

    @Override
    public long totalQteDrugHavingCommand(String pseudo) {
        Command command = commandRepository.findDistinctByPseudo(pseudo);
        if (command == null) {
            throw new RuntimeException("Commande introuvable : " + pseudo);
        }
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());
        return commandRepository.totalQteDrugHavingCommand(pseudo);
    }

    @Override
    public boolean existById(long id) {
        return commandRepository.existsById(id);
    }

    @Override
    public CommandeResponseDto updateCommande(long id, CommandeRequestDto dto) {
        Command command = commandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        // ✅ Sécurité : on vérifie l'appartenance AVANT de modifier
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());

        // ✅ Sécurité : un non-admin ne peut pas transférer sa commande à un autre
        String targetUserName = dto.getUserName();
        if (!securityUtils.isCurrentUserAdmin()) {
            String currentUserName = securityUtils.getCurrentUserName();
            if (!currentUserName.equals(targetUserName)) {
                throw new com.formation.pharmacy_manager.exceptions.ForbiddenException(
                        "Vous ne pouvez pas transférer une commande à un autre utilisateur.");
            }
        }

        User user = userRepository.findDistinctByUserName(targetUserName);
        if (user == null) {
            throw new RuntimeException("Utilisateur introuvable : " + targetUserName);
        }

        command.setPseudo(dto.getPseudo());
        command.setUser(user);
        command.setCreation_date(new Date());

        Command cmd = commandRepository.save(command);
        return toDto(cmd);
    }

    // ===========================
    // Méthode utilitaire
    // ===========================
    private CommandeResponseDto toDto(Command cmd) {
        return new CommandeResponseDto(
                cmd.getCommandId(),
                cmd.getPseudo(),
                cmd.getUser().getUserName(),
                cmd.getCreation_date()
        );
    }
}