package net.md_5.chunkr.plugin;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
public abstract class Command
{

    private final String name;
    private final String[] aliases;

    public Command(String name)
    {
        this( name, (String[]) null );
    }

    public Command(String name, String... aliases)
    {
        Preconditions.checkArgument( name != null, "name" );
        this.name = name;
        this.aliases = aliases;
    }

    public abstract void execute(String[] args);
}
